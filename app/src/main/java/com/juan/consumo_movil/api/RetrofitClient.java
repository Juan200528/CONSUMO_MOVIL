package com.juan.consumo_movil.api;

import android.content.Context;

import com.juan.consumo_movil.utils.AuthInterceptor;
import com.juan.consumo_movil.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://backend-nrpu.onrender.com/ ";
    private static volatile Retrofit retrofit = null; // Asegúrate de mantener esto para thread safety
    private static volatile ApiService apiService = null;
    private static Context appContext;

    /**
     * Inicializa el contexto de la aplicación.
     * Debe llamarse antes de usar RetrofitClient, idealmente en Application o MainActivity.
     */
    public static synchronized void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context no puede ser nulo.");
        }
        appContext = context.getApplicationContext();

        // ✅ Inicializamos SessionManager aquí también
        SessionManager.init(appContext); // 👈 Llamada al método init del SessionManager
    }

    /**
     * Retorna la instancia de ApiService configurada.
     * @return ApiService para hacer llamadas al backend.
     */
    public static ApiService getApiService() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) { // Double-checked locking
                    if (appContext == null) {
                        throw new IllegalStateException(
                                "RetrofitClient no ha sido inicializado. Llama a RetrofitClient.init(context) antes de usarlo."
                        );
                    }

                    // ✅ Usamos el Singleton de SessionManager
                    SessionManager sessionManager = SessionManager.getInstance(); // ✅ No usamos new SessionManager()

                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(sessionManager)) // Ahora sí funciona
                            .addInterceptor(loggingInterceptor)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}