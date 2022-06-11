//package IntegrationTesting;
//
//import Domain.GuidingBasketsManagement.GuidingBasketController;
//import Domain.GuidingBasketsManagement.GuidingBasketDTO;
//import Domain.UsersManagment.UserController;
//import Domain.UsersManagment.UserStateEnum;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Arrays;
//
//public class GuidingBasketIntegrationTests {
//
//    private GuidingBasketDTO basketDTO;
//    private final GuidingBasketController guidingBasketController = GuidingBasketController.getInstance();
//
//    @Before
//    public void setUp(){
//        basketDTO = new GuidingBasketDTO("0", "Hello", "There", Arrays.asList("General", "Kenobi"));
//    }
//
//    @Test
//    public void addBasketSuccess(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//        userController.logout(adminName);
//        userController.login("Dvorit");
//
//        Assert.assertFalse(guidingBasketController.addBasket("Dvorit", basketDTO).isFailure());
//    }
//
////    @Test
////    public void addBasketNotLoggedInFailure(){
////        UserController userController = UserController.getInstance();
////        String guestName = userController.addGuest().getResult();
////        String adminName = userController.login(guestName, "admin", "admin").getResult().getFirst();
////        userController.registerUserByAdmin(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
////        userController.logout(adminName);
////
////        Assert.assertTrue(guidingBasketController.addBasket("Dvorit", basketDTO).isFailure());
////    }
//
//    @Test
//    public void removeBasketSuccess(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//
//        Assert.assertFalse(guidingBasketController.addBasket("Dvorit", basketDTO).isFailure());
//    }
//
//    @Test
//    public void removeBasketNoPermissionsFailure(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//        userController.registerUserBySystemManager(adminName, "Miri", "Band", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//
//        Assert.assertTrue(guidingBasketController.removeBasket("Miri", basketDTO).isFailure());
//    }
//
//    @Test
//    public void setTitleSuccess(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//        guidingBasketController.addBasket("Dvorit", basketDTO);
//
//        Assert.assertFalse(guidingBasketController.setBasketTitle("Dvorit", basketDTO, "newTitle").isFailure());
//    }
//
//    @Test
//    public void setTitleNoPermissionsFailure(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//        userController.registerUserBySystemManager(adminName, "Miri", "Band", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//
//        Assert.assertTrue(guidingBasketController.setBasketTitle("Miri", basketDTO, "new Title").isFailure());
//    }
//
//    @Test
//    public void addBasketLabelSuccess(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//        guidingBasketController.addBasket("Dvorit", basketDTO);
//
//        Assert.assertFalse(guidingBasketController.addBasketLabel("Dvorit", basketDTO, "label").isFailure());
//    }
//
//    @Test
//    public void addBasketLabelNoPermissionsFailure(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//        userController.registerUserBySystemManager(adminName, "Miri", "Band", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//
//        Assert.assertTrue(guidingBasketController.addBasketLabel("Miri", basketDTO, "label").isFailure());
//    }
//
//    @Test
//    public void removeBasketLabelSuccess(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//        guidingBasketController.addBasket("Dvorit", basketDTO);
//
//        guidingBasketController.addBasketLabel("Dvorit", basketDTO, "label");
//        Assert.assertFalse(guidingBasketController.removeBasketLabel("Dvorit", basketDTO, "label").isFailure());
//    }
//
//    @Test
//    public void removeBasketLabelNoPermissionsFailure(){
//        UserController userController = UserController.getInstance();
//        String adminName = userController.login("admin").getResult();
//        userController.registerUserBySystemManager(adminName, "Dvorit", "Dvorit", UserStateEnum.SUPERVISOR, "", "tech", "", "", "","", "");
//        userController.registerUserBySystemManager(adminName, "Miri", "Band", UserStateEnum.SUPERVISOR, "", "tech", "", "", "", "", "");
//
//        userController.logout(adminName);
//        userController.login("Dvorit");
//        guidingBasketController.addBasketLabel("Dvorit", basketDTO, "label");
//
//        Assert.assertTrue(guidingBasketController.removeBasketLabel("Miri", basketDTO, "label").isFailure());
//    }
//}
