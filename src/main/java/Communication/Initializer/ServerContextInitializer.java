package Communication.Initializer;

public class ServerContextInitializer {

    private boolean mockMode;
    private boolean testMode;
    private String dbConnection;
    private String dbUsername;
    private String dbPassword;

    private static class CreateSafeThreadSingleton {
        private static final ServerContextInitializer INSTANCE = new ServerContextInitializer();
    }

    public static ServerContextInitializer getInstance() {
        return ServerContextInitializer.CreateSafeThreadSingleton.INSTANCE;
    }

    public ServerContextInitializer() {
        this.mockMode = false;
        this.testMode = false;
//        this.dbConnection = "jdbc:postgresql://tai.db.elephantsql.com:5432/pxbghxfm";
//        this.dbUsername = "pxbghxfm";
//        this.dbPassword = "ogms2UJpzqjopRw29YcJ5Wau7wHQLkcJ";

        this.dbConnection = "jdbc:postgresql://ec2-54-228-32-29.eu-west-1.compute.amazonaws.com:5432/dddpg4sf4g3pmg";
        this.dbUsername = "zewqiehdaufwut";
        this.dbPassword = "09bf3e7925e4568c6f0483fd467da47331296c87ff5deb63d1fac3f6912437df";
    }

    public void setMockMode() {
        this.mockMode = true;

        this.dbConnection = "jdbc:postgresql://localhost:5432/maaneDBMock";
        this.dbUsername = "postgres";
        this.dbPassword = "1234";

//        this.dbConnection = "jdbc:postgresql://localhost:5432/MAANE";
//        this.dbUsername = "postgres";
//        this.dbPassword = "1234";
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode() {
        this.testMode = true;
    }

    public void setTestMode(boolean mode) {
        this.testMode = mode;
    }

    public void setMockMode(boolean mode) {this.mockMode = mode; }

    public boolean isMockMode(){
        return this.mockMode;
    }

    public String getDbConnection() {
        return dbConnection;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}