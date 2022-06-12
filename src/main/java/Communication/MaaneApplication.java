package Communication;

import Communication.DTOs.*;
import Communication.Initializer.ServerContextInitializer;
import Communication.Security.KeyLoader;
import Communication.Service.UserServiceImpl;
import Domain.DataManagement.AnswerState.AnswerType;
import Domain.DataManagement.DataController;
import Domain.DataManagement.FaultDetector.Rules.*;
import Domain.DataManagement.SurveyController;
import Domain.UsersManagment.UserController;
import Domain.UsersManagment.UserStateEnum;
import Domain.WorkPlan.GoalsManagement;
import Persistence.DbDtos.SchoolDBDTO;
import Persistence.UserQueries;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

@SpringBootApplication
public class MaaneApplication {

	public static void main(String[] args) {
//		ServerContextInitializer.getInstance().setMockMode();

		SpringApplication.run(MaaneApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
	}

	@Bean
	CommandLineRunner run(UserServiceImpl service){
		return args -> {

			if(ServerContextInitializer.getInstance().isMockMode()){
				System.out.println("Mock Mode is Activated!");

				UserQueries.getInstance().clearDB();

				UserController userController = UserController.getInstance();
				userController.login("admin");

				service.registerUserBySystemManager("admin", new UserDTO("admin", "tech", "ronit", "1234abcd", UserStateEnum.SUPERVISOR,
						"ronit", "newe", "ronit@gmail.com", "055-555-5555", "", null), "");

				service.registerUserBySystemManager("admin", new UserDTO("admin", "tech", "tal", "1234abcd", UserStateEnum.INSTRUCTOR,
						"tal", "kadosh", "tal@gmail.com", "055-555-5555", "", null), "ronit");

				service.registerUserBySystemManager("admin", new UserDTO("admin", "tech", "shaked", "1234abcd", UserStateEnum.INSTRUCTOR,
						"shaked", "cohen", "shaked@gmail.com", "055-555-5555", "", null), "ronit");

				DataController.getInstance().loadSchoolsToDB();

				DataController.getInstance().insertSchool(new SchoolDBDTO("1111111", "testing school", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 30));

				DataController.getInstance().insertSchool(new SchoolDBDTO("2222222", "testing school2", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 31));

				DataController.getInstance().insertSchool(new SchoolDBDTO("3333333", "testing school3", "beer sheva", "", "", "", "", "", "", "", "", 1000000, "", "", "", "", 32));

				DataController.getInstance().assignCoordinator("admin", "tech", "aviad", "shal", "aviad@gmail.com", "0555555555", "1111111");

				userController.logout("admin");

				userController.login("ronit");

				String school1 = "1111111";
				String school2 = "2222222";
				String school3 = "33333333";

				userController.assignSchoolToUser("ronit", "tal", school1);
				userController.assignSchoolToUser("ronit", "shaked", school2);
				userController.assignSchoolToUser("ronit", "shaked", school3);

				// create survey
				SurveyDTO surveyDTO = new SurveyDTO(false, "1111", "survey1", "description",
						Arrays.asList("symbol", "open?", "numeric?", "multiple choice?"),
						Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), Arrays.asList("correct", "wrong")),
						Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE), 2022);

				SurveyController.getInstance().createSurvey("ronit", surveyDTO);
				UserController.getInstance().createSurvey("ronit", surveyDTO.getId());

				// create goals
				GoalDTO goalDTO1 = new GoalDTO(1, "yahad1", "", 1,
						5, "tech", 2022);
				GoalDTO goalDTO2 = new GoalDTO(2, "yahad2", "", 2,
						10, "tech",2022);

				GoalsManagement.getInstance().addGoalToField("tech", goalDTO1, 2022);
				GoalsManagement.getInstance().addGoalToField("tech", goalDTO2, 2022);

				Rule rule1 = new AndRule(Arrays.asList(new NumericBaseRule(2, Comparison.EQUAL, 40),
						new MultipleChoiceBaseRule(3, List.of(1))));

				Rule rule2 = new NumericBaseRule(2, Comparison.EQUAL, 30);

				// create rules
				SurveyController.getInstance().addRule("ronit", "1111", rule1, 1);
				SurveyController.getInstance().addRule("ronit", "1111", rule2, 2);

				// submit survey
				SurveyController.getInstance().submitSurvey("ronit", "1111");

				// add answers
				SurveyController.getInstance().addAnswers(new SurveyAnswersDTO("1111",
						new LinkedList<>(Arrays.asList("1111111", "open ans","30", "0")),
						new LinkedList<>(Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE))));

				SurveyController.getInstance().addAnswers(new SurveyAnswersDTO("1111",
						new LinkedList<>(Arrays.asList("2222222", "open ans", "40", "1")),
						new LinkedList<>(Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE))));

				// create another survey
				surveyDTO = new SurveyDTO(true, "2222", "title", "description",
						Arrays.asList("symbol", "open?", "numeric?", "multiple choice?"),
						Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), Arrays.asList("correct", "wrong")),
						Arrays.asList(AnswerType.NUMERIC_ANSWER, AnswerType.OPEN_ANSWER, AnswerType.NUMERIC_ANSWER, AnswerType.MULTIPLE_CHOICE), 2022);

				SurveyController.getInstance().createSurvey("ronit", surveyDTO);
				UserController.getInstance().createSurvey("ronit", surveyDTO.getId());

				userController.logout("ronit");

				userController.login("tal");
				userController.setWorkingTime("tal", 0, LocalTime.of(8, 30).toString(), LocalTime.of(10, 30).toString(), LocalTime.of(11, 0).toString(), LocalTime.of(13, 0).toString());
				userController.logout("tal");

				userController.login("shaked");
				userController.setWorkingTime("shaked", 3, LocalTime.of(10, 0).toString(), LocalTime.of(12, 0).toString(), LocalTime.of(13, 0).toString(), LocalTime.of(15, 0).toString());
				userController.logout("shaked");
			}
			else {
				System.out.println("The server is running!");
//				UserQueries.getInstance().clearDB();
//				DataController.getInstance().loadSchoolsToDB();
			}
		};
	}


}
