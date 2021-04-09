package fitnessapp_objects;

// All the firebase calling action will stay in this class, Singleton class
public class DataBase {

    private static DataBase dataBase_instance = null;

    private DataBase(){

    }

    public static DataBase getInstance(){

        if (dataBase_instance == null) dataBase_instance = new DataBase();
        return dataBase_instance;

    }

    public boolean storeUserAccount(UserAccount account){

        return false;
    }

    public boolean storeChallengeRoom(ChallengeRoom room){

        return false;
    }

    public boolean getUserAccount(String userID){

        return false;
    }

    public boolean getChallengeRoom(String roomID){

        return false;
    }

}
