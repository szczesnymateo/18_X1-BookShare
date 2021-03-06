package pl.lodz.p.whoborrowedthat.service;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.lodz.p.whoborrowedthat.coverter.CustomConverterFactory;
import pl.lodz.p.whoborrowedthat.model.Stuff;
import pl.lodz.p.whoborrowedthat.model.User;
import pl.lodz.p.whoborrowedthat.model.UserRelation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private static AuthApi authService;
    private static DataApi dataService;
    private static ApiManager apiManager;

    public enum StuffType { BORROWED, LENT }

    private ApiManager() {

        final String END_POINT = "https://who-borrowed-that.herokuapp.com/";

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(END_POINT)
                .addConverterFactory(new CustomConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        authService = retrofit.create(AuthApi.class);
        dataService = retrofit.create(DataApi.class);

    }

    public static ApiManager getInstance() {
        if (apiManager == null) {
            apiManager = new ApiManager();
        }
        return apiManager;
    }

    public void loginUser(String email, String password, Callback<User> callback) {
        Call<User> userCall = authService.login(email,password);
        userCall.enqueue(callback);
    }

    public void getStuff(StuffType stuffType, User user, Callback<List<Stuff>> callback) {
        switch (stuffType) {
            case BORROWED:
                getBorrowedStuff(user, callback);
                break;
            case LENT:
                getLentStuff(user, callback);
                break;
        }
    }

    private void getLentStuff(User user, Callback<List<Stuff>> callback) {
        dataService.getLentThingsByUserEmail(user.getToken(), user.getEmail()).enqueue(callback);
    }

    private void getBorrowedStuff(User user, Callback<List<Stuff>> callback) {
        dataService.getBorrowedThingsByUserEmail(user.getToken(), user.getEmail()).enqueue(callback);
    }

    public void registerUser(String email, String password, String passwordConfirmation,String username,  Callback<User> callback) {
        Call<User> userCall = authService.register(email, password, passwordConfirmation, username);
        userCall.enqueue(callback);
    }

    public void notifyBorrow(User user, long id, Callback<Object> callback) {
        Call<Object> queue = dataService.notify(user.getToken(), user.getEmail(), id, true);
        queue.enqueue(callback);
    }
    public void readNotification(User user, long id, Callback<Object> callback) {
        Call<Object> queue = dataService.notify(user.getToken(), user.getEmail(), id, false);
        queue.enqueue(callback);
    }

    public void addBorrows(User user, Stuff stuff, Callback<Object> callback) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat a  = new SimpleDateFormat("dd-MM-YYYY");
        Call<Object> userCall = dataService.addBorrows(user.getToken(), user.getEmail(),
                String.valueOf(user.getId()), String.valueOf(stuff.getBorrower().getId()),
                stuff.getName(),a.format(stuff.getRentalDate()), a.format(stuff.getEstimatedReturnDate()));
        userCall.enqueue(callback);
    }

    public void getUserRelations(User user, Callback<List<UserRelation>> callback) {
        dataService.getUserRelations(user.getToken(), user.getEmail()).enqueue(callback);
    }

    public void setUserRelation(User user, String emailToAdd, Callback<UserRelation> callback) {
        dataService.setUserRelation(user.getToken(), user.getEmail(), emailToAdd).enqueue(callback);
    }

    public void removeUserRelation(User user, long urID, Callback<Void> callback) {
        dataService.removeUserRelation(urID, user.getToken(), user.getEmail()).enqueue(callback);
    }

    public void updateUsername(User user, String username, Callback<Void> callback) {
        Call<Void> queue = authService.setUsername(user.getToken(), user.getEmail(), username);
        queue.enqueue(callback);
    }

    public void changeEstimatedReturnDate(User user, long borrowID, Date newDate, Callback<Void> callback) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat a  = new SimpleDateFormat("dd-MM-YYYY");
        Call<Void> queue = dataService.changeReturnDate(borrowID, user.getToken(), user.getEmail(), a.format(newDate));
        queue.enqueue(callback);
    }

    public void returnItem(User user, long borrowID, Date date, Callback<Void> callback) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat a  = new SimpleDateFormat("dd-MM-YYYY");
        Call<Void> queue = dataService.returnItem(borrowID, user.getToken(), user.getEmail(), a.format(date));
        queue.enqueue(callback);
    }
}