package Domain.MonthlyReport;

import Domain.CommonClasses.Response;
import Domain.UsersManagment.APIs.DTOs.UserActivityInfoDTO;
import Domain.UsersManagment.APIs.DTOs.UserInfoDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.tomcat.jni.Local;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MSWordWriterService {

    /**
     * create docx file according to monthly report template
     * @param filepath the location the file will be saved and manipulated
     * @return response if the function succeeded
     */
    public Response<File> createDoc(String filepath, UserInfoDTO userInfo, List<UserActivityInfoDTO> activities, LocalDateTime date, int year, int month) {
        XWPFDocument document = new XWPFDocument();

        setPageSize(document, 15840, 12240);
        addFooter(document);
        addHeader(document);
        XWPFTable userDetailsTable = addUserDetailsTable(document, userInfo.getWorkingDay());
        fillUserDetailsTable(userDetailsTable, userInfo, year, month);

        document.createParagraph();
        XWPFTable activitiesTable = addActivityTable(document, activities.size() + 4);
        fillActivitiesTable(activitiesTable, activities);

        document.createParagraph();
        addApprovalTable(document, date);

        try {
            document.write(new FileOutputStream(filepath));
            document.close();
        } catch(IOException e){
            return new Response<>(null, true, e.getMessage());
        }

        return new Response<>(new File(filepath), false, "monthly report generated successfully");
    }


    /**
     * Set page size to given width and height
     */
    private void setPageSize(XWPFDocument document, int width, int height){
        CTDocument1 doc = document.getDocument();
        CTBody body = doc.getBody();

        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();

        if(!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();

        pageSize.setW(BigInteger.valueOf(width));
        pageSize.setH(BigInteger.valueOf(height));
    }

    private void addFooter(XWPFDocument document) {
        XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = header.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("מדינת ישראל");
        run.addBreak();
        run.setText(" משרד החינוך");
        run.setFontFamily("Arial");
        run.setFontSize(12);
        run.setBold(true);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
    }

    private void addHeader(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("דוח עבודה חדשי במסגרת ההדרכה לעובדי הוראה/גני ילדים: מורה בתפקיד הדרכה במחוז");
        run.setFontFamily("David");
        run.setFontSize(14);
        run.setBold(true);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
    }

    public XWPFTable addUserDetailsTable(XWPFDocument document, int workingDay) {
        XWPFTable table = document.createTable(2, 32);
        table.setWidth(13500);

        // first row
        table.getRow(0).setHeight(500);

        mergeTableCellsHorizontal(table,0, 0, 3);
        writeIntoTableCell(table.getRow(0).getCell(0), "מקום המגורים", "", true, ParagraphAlignment.RIGHT, 12);

        mergeTableCellsHorizontal(table,0, 3, 5);
        writeIntoTableCell(table.getRow(0).getCell(3), "השנה", "", true, ParagraphAlignment.RIGHT, 12);

        writeIntoTableCell(table.getRow(0).getCell(5), "החודש", "", true, ParagraphAlignment.RIGHT, 12);

        for(int i = 6; i < 15; i++)
            table.getRow(0).getCell(i).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(300));

        writeIntoTableCell(table.getRow(0).getCell(15), "מס ת\"ז", "", true, ParagraphAlignment.RIGHT, 12);

        mergeTableCellsHorizontal(table,0, 16, 24);
        writeIntoTableCell(table.getRow(0).getCell(16), "השם הפרטי", "", true, ParagraphAlignment.RIGHT, 12);

        mergeTableCellsHorizontal(table,0, 24, 32);
        writeIntoTableCell(table.getRow(0).getCell(24), "שם המשפחה", "", true, ParagraphAlignment.RIGHT, 12);


        // second row
        table.getRow(1).setHeight(1000);

        mergeTableCellsHorizontal(table,1, 0, 5);
        writeIntoTableCell(table.getRow(1).getCell(0), "היחידה/המחוז ", "המינהל לחינוך התיישבותי", true, ParagraphAlignment.RIGHT, 12);

        mergeTableCellsHorizontal(table,1, 5, 17);
        writeIntoTableCell(table.getRow(1).getCell(5), ":נושא ההדרכה", "", true, ParagraphAlignment.RIGHT, 12);

        for(int i = 0; i < 6; i++)
            writeIntoTableCell(table.getRow(1).getCell(22 - i), (char)('א' + i)+"", i == workingDay? "X": "O", true, ParagraphAlignment.CENTER, 12);

        mergeTableCellsHorizontal(table,1, 23, 27);
        writeIntoTableCell(table.getRow(1).getCell(23), "היום בשבוע", "(יש לסמן X)", true, ParagraphAlignment.CENTER, 12);

        mergeTableCellsHorizontal(table,1, 27, 32);
        writeIntoTableCell(table.getRow(1).getCell(27), "מספר ימי ההדרכה", "", true, ParagraphAlignment.RIGHT, 12);

        return table;
    }

    private void writeIntoTableCell(XWPFTableCell cell, String text, String subText,  boolean bold, ParagraphAlignment alignment, int fontSize) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(fontSize);
        paragraph.setAlignment(alignment);
        run.setBold(bold);
        run.setText(text);

        if(subText.length() > 0) {
            run.addBreak();
            run.addBreak();
            run.setText(subText);
        }

    }


    private void addApprovalTable(XWPFDocument document, LocalDateTime date) {

        XWPFTable table = document.createTable(1, 2);
        table.setWidth(13500);

        XWPFTableCell cell = table.getRow(0).getCell(0);

        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(10);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText(":הריני מאשר שהעובד הועסק ונסע באישור בהתאם לרשום לעיל" );
        run.addBreak();
        run.setText(" נסע בתפקיד כרשום לעיל - " );
        run.addBreak();
        run.setText(" מגיעות לו הוצאות אש\"ל ודמי כלכלה - " );

        paragraph = cell.addParagraph();
        run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(12);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText(" __________ :תאריך    _________ :שם  _______________ :חתימה  ");


        cell = table.getRow(0).getCell(1);

        paragraph = cell.getParagraphs().get(0);
        run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(10);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText(".אני מצהיר בזאת כי בתאריכים הנ\"ל עבדתי ונסעתי בהתאם לרשום לעיל" );
        run.addBreak();
        run.setText(".האש\"ל והוצאות הנסיעה הנדרשים על ידי לעיל לא שולמו לי ולא נדרשו משום גוף נוסף" );
        run.addBreak();

        paragraph = cell.addParagraph();
        run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(12);
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText("______________:" + "תאריך: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "   חתימה ");

    }


    public XWPFTable addActivityTable(XWPFDocument document, int numRows) {
        XWPFTable table = document.createTable(numRows + 3, 17);
        table.setWidth(13500);

        // first row
        mergeTableCellsHorizontal(table,0, 0, 3);
        writeIntoTableCell(table.getRow(0).getCell(0), "אש\"ל ודמי כלכלה", "", true, ParagraphAlignment.CENTER, 10);

        mergeTableCellsHorizontal(table,0, 3, 5);
        writeIntoTableCell(table.getRow(0).getCell(3), "דמי הנסיעה ברכב ציבורי", "", true, ParagraphAlignment.CENTER, 12);

        mergeTableCellsHorizontal(table,0, 5, 7);
        writeIntoTableCell(table.getRow(0).getCell(5), "המרחק בקילומטרים", "", true, ParagraphAlignment.CENTER, 12);

        mergeTableCellsHorizontal(table,0, 7, 9);
        writeIntoTableCell(table.getRow(0).getCell(7), "יעדי הנסיעה", "", true, ParagraphAlignment.CENTER, 12);

        writeIntoTableCell(table.getRow(0).getCell(9), "הצוות המודרך", "", true, ParagraphAlignment.CENTER, 12);

        writeIntoTableCell(table.getRow(0).getCell(10), "מקום הפעילות/שם בית הספר", "", true, ParagraphAlignment.CENTER, 12);

        writeIntoTableCell(table.getRow(0).getCell(11), "תיאור הפעילות", "", true, ParagraphAlignment.CENTER, 12);

        writeIntoTableCell(table.getRow(0).getCell(12), "סה\"כ השעות", "", true, ParagraphAlignment.CENTER, 12);

        mergeTableCellsHorizontal(table,0, 13, 15);
        writeIntoTableCell(table.getRow(0).getCell(13), "שעות העבודה", "", true, ParagraphAlignment.CENTER, 12);

        writeIntoTableCell(table.getRow(0).getCell(15), "תאריך", "", false, ParagraphAlignment.CENTER, 10);

        writeIntoTableCell(table.getRow(0).getCell(16), "יום", "", false, ParagraphAlignment.CENTER, 10);


        // second row
        writeIntoTableCell(table.getRow(1).getCell(0), "ערב", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(1), "צהריים", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(2), "בוקר", "", false, ParagraphAlignment.CENTER, 10);

        writeIntoTableCell(table.getRow(1).getCell(3), "עירוני", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(4), "בינעירוני", "", false, ParagraphAlignment.CENTER, 10);

        writeIntoTableCell(table.getRow(1).getCell(5), "דרכים קשות", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(6), "דרכים רגילות", "", false, ParagraphAlignment.CENTER, 10);

        writeIntoTableCell(table.getRow(1).getCell(7), "עד מקום", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(8), "ממקום", "", false, ParagraphAlignment.CENTER, 10);

        writeIntoTableCell(table.getRow(1).getCell(13), "עד שעה", "", false, ParagraphAlignment.CENTER, 10);
        writeIntoTableCell(table.getRow(1).getCell(14), "משעה", "", false, ParagraphAlignment.CENTER, 10);

        // update last row - total
        mergeTableCellsHorizontal(table,numRows + 2, 9, 17);
        writeIntoTableCell(table.getRow(numRows + 2).getCell(9),"סה\"כ", "", true, ParagraphAlignment.RIGHT, 10);

        // merging columns
        mergeTableCellsVertical(table,9);
        mergeTableCellsVertical(table,10);
        mergeTableCellsVertical(table,11);
        mergeTableCellsVertical(table,12);
        mergeTableCellsVertical(table,15);
        mergeTableCellsVertical(table,16);

        return table;
    }

    private void mergeTableCellsHorizontal(XWPFTable table, int rowIdx, int startColIdx, int endColIdx) {

        for(int i = startColIdx + 1; i < endColIdx; i++) {
            // first column
            CTHMerge hMerge = CTHMerge.Factory.newInstance();
            hMerge.setVal(STMerge.RESTART);
            XWPFTableCell cell = table.getRow(rowIdx).getCell(startColIdx);

            if (cell.getCTTc().getTcPr() == null)
                cell.getCTTc().addNewTcPr();

            cell.getCTTc().getTcPr().setHMerge(hMerge);

            // the collapsed  column
            CTHMerge hMerge1 = CTHMerge.Factory.newInstance();
            hMerge1.setVal(STMerge.CONTINUE);
            cell = table.getRow(rowIdx).getCell(i);

            if (cell.getCTTc().getTcPr() == null)
                cell.getCTTc().addNewTcPr();

            cell.getCTTc().getTcPr().setHMerge(hMerge1);
        }

    }

    private void mergeTableCellsVertical(XWPFTable table, int colIdx) {

        // first column
        CTVMerge vMerge = CTVMerge.Factory.newInstance();
        vMerge.setVal(STMerge.RESTART);
        XWPFTableCell cell = table.getRow(0).getCell(colIdx);

        if (cell.getCTTc().getTcPr() == null)
            cell.getCTTc().addNewTcPr();

        cell.getCTTc().getTcPr().setVMerge(vMerge);

        // the collapsed  column
        CTVMerge  vMerge1  = CTVMerge.Factory.newInstance();
        vMerge1.setVal(STMerge.CONTINUE);
        cell = table.getRow(1).getCell(colIdx);

        if (cell.getCTTc().getTcPr() == null)
            cell.getCTTc().addNewTcPr();

        cell.getCTTc().getTcPr().setVMerge(vMerge1);

    }

    private void fillUserDetailsTable(XWPFTable table, UserInfoDTO userInfo, int year, int month) {

        insertAnswer(table.getRow(0).getCell(0), userInfo.getCity());
        insertAnswer(table.getRow(0).getCell(3), year + "");
        insertAnswer(table.getRow(0).getCell(5), month + "");
        insertAnswer(table.getRow(0).getCell(16), userInfo.getFirstName());
        insertAnswer(table.getRow(0).getCell(24), userInfo.getLastName());

    }

    private void fillActivitiesTable(XWPFTable table, List<UserActivityInfoDTO> activities) {

        int row = 2;

        for(UserActivityInfoDTO userActivityInfoDTO: activities){
            insertActivity(table, userActivityInfoDTO, row);
            row++;
        }

    }

    private void insertActivity(XWPFTable table, UserActivityInfoDTO userActivityInfoDTO, int row) {

        LocalDateTime start, end;
        double totalTime;

        insertActivity(table.getRow(row).getCell(7), userActivityInfoDTO.getSchoolCity());
        insertActivity(table.getRow(row).getCell(8), userActivityInfoDTO.getUserCity());
        insertActivity(table.getRow(row).getCell(10), userActivityInfoDTO.getSchoolName());

        // set time
        start = userActivityInfoDTO.getActivityStart();
        end = userActivityInfoDTO.getActivityEnd();
        totalTime = ChronoUnit.MINUTES.between(start, end);

        String minutes = (int)(totalTime % 60) + "";
        if(minutes.length() == 1)
            minutes = "0"+minutes;
        else
            minutes = minutes.substring(0,2);

        insertActivity(table.getRow(row).getCell(12),  (int)(totalTime / 60) + ":" + minutes);

        insertActivity(table.getRow(row).getCell(13), end.format(DateTimeFormatter.ofPattern("HH:mm")));
        insertActivity(table.getRow(row).getCell(14), start.format(DateTimeFormatter.ofPattern("HH:mm")));

        insertActivity(table.getRow(row).getCell(15), start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        insertActivity(table.getRow(row).getCell(16), dayToChar(start.getDayOfWeek().getValue()));


    }

    private String dayToChar(int dayOffset) {
        int day = dayOffset % 7;

        return day == 6 ? "ש" : (char)('א' + day) + "";
    }

    private void insertAnswer(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(12);
        run.setColor("2C2C2C");
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText(text);
    }

    private void insertActivity(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();

        run.setFontFamily("David");
        run.setFontSize(10);
        run.setColor("2C2C2C");
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        run.setText(text);
    }




}