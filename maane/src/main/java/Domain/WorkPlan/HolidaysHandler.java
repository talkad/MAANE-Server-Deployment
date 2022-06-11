package Domain.WorkPlan;

import Communication.DTOs.ActivityDTO;
import Domain.CommonClasses.Pair;
import Domain.CommonClasses.Response;
import Persistence.HolidaysQueries;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HolidaysHandler {
    int year;
    String informationStringYear1;
    String informationStringYear2;
    ArrayList<String[]> informationArray;

    public HolidaysHandler(int year){
        this.year = year;
        this.informationArray = new ArrayList<>();
        init(year);
    }

    private void init(int year){
        if (holidaysForYearExists(year) & holidaysForYearExists(year + 1)){
            informationArray = getHolidaysForYear(year);
        }

        else {
            informationStringYear1 = fetchAPI(year);
            informationStringYear2 = fetchAPI(year + 1);
            fillArray(informationStringYear1);
            fillArray(informationStringYear2);
            writeToDb();
        }
    }

    private String fetchAPI(int year) {
        StringBuilder informationString = new StringBuilder();
        try {
            URL url = new URL("https://www.hebcal.com/hebcal?v=1&cfg=json&maj=on&mod=on&year="+year+"&c=off&month=x&geonameid=293397");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();

            // 200 OK
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                //informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }
                //Close the scanner
                scanner.close();
            }
        } catch (Exception e) {return "Bad fetch";}
        return informationString.toString();
    }

    private void fillArray(String inputString){
        if(informationStringYear1.equals("Bad fetch") | informationStringYear2.equals("Bad fetch"))return;
        JsonObject gsonObj = new Gson().fromJson(inputString, JsonObject.class);
        JsonArray jsonArray = gsonObj.getAsJsonArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
            String title = jsonArray.get(i).getAsJsonObject().get("hebrew").getAsString();
            String date = jsonArray.get(i).getAsJsonObject().get("date").getAsString();
            String category = jsonArray.get(i).getAsJsonObject().get("category").getAsString();
            if (!isSpecificHoliday(title) && !category.equals("candles")){
                this.informationArray.add(new String[]{title, date, category});
            }
        }
    }

//    private boolean isSpecificHoliday(String title){
//        return title.startsWith("Havdalah") || title.startsWith("Yom HaAliyah") ||
//                title.startsWith("Yom HaShoah") || title.startsWith("Yom Yerushalayim") ||
//                title.startsWith("Fast begins") | title.startsWith("Erev Tish'a B'Av") || title.startsWith("Tish'a B'Av (observed)") ||
//                title.startsWith("Fast ends") || title.startsWith("Shmini Atzeret") ||
//                title.startsWith("Sigd");//title.startsWith("Yom HaAliyah School Observance") |
//
//    }

    private boolean isSpecificHoliday(String title){
        return title.startsWith("הבדלה") || title.startsWith("יום העליה") ||
                title.startsWith("יום השואה") || title.startsWith("יום ירושלים") ||
                title.startsWith("תחילת הצום") | title.startsWith("ערב תשעה באב") || title.startsWith("תשעה באב נדחה") ||
                title.startsWith("סיום הצום") || title.startsWith("שמיני עצרת") ||
                title.startsWith("סיגד");//title.startsWith("Yom HaAliyah School Observance") |

    }

    private void writeToDb(){
        HolidaysQueries.getInstance().insertHolidaysDates(informationArray);
    }

    public ArrayList<String[]> getHolidaysForYear(int year){
        if (informationArray.isEmpty()){
            informationArray = HolidaysQueries.getInstance().getHolidaysDates(year);
            informationArray.addAll(HolidaysQueries.getInstance().getHolidaysDates(year + 1));
        }
        return informationArray;
    }

    public void printArray(){
        for (String[] entry : informationArray) {
            System.out.println(entry[0] + " on date: " + entry[1] + " and category " + entry[2]);
        }
    }

    // returns if database already contains holidays or year, and we don't need to fetch API
    public boolean holidaysForYearExists (int year){
        return HolidaysQueries.getInstance().holidaysForYearExists(year);
    }

    public boolean isHoliday(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDateTime = localDateTime.format(formatter);
        for (String [] entry : informationArray) {
            String date = entry[1].substring(0,10);
            if (date.equals(formattedDateTime)){
                return true;
            }
        }
        return false;
    }

    public Response<List<Pair<LocalDateTime, ActivityDTO>>> getHolidaysAsActivity(int year, int month){
        return HolidaysQueries.getInstance().getHolidaysAsActivity(year, month);
    }
}
