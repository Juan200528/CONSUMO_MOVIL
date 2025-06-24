package com.juan.consumo_movil.api;

import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Asistente;
import com.juan.consumo_movil.model.LoginResponse;
import com.juan.consumo_movil.model.User;
import com.juan.consumo_movil.models.PromotionRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 🔐 Registro de usuario
    @POST("api/auth/register")
    Call<LoginResponse> register(@Body User user);

    // 🔑 Inicio de sesión
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body User user);

    // 🚪 Cerrar sesión
    @POST("api/auth/logout")
    Call<Void> logout();

    // 👤 Actualizar datos del usuario
    @PUT("users/{id}")
    Call<User> updateUser(
            @Path("id") int id,
            @Body User user,
            @Header("Authorization") String token
    );

    // 📋 Obtener lista de mis actividades
    @GET("api/tasks")
    Call<List<ActividadModel>> obtenerActividades(@Header("Authorization") String token);

    // ➕ Crear nueva actividad con imagen
    @Multipart
    @POST("api/tasks")
    Call<ActividadModel> crearActividadConImagen(
            @Header("Authorization") String authToken,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("date") RequestBody date,
            @Part("place") RequestBody place,
            @Part("responsible") RequestBody responsible,
            @Part MultipartBody.Part image
    );

    // 🧑‍🤝‍🧑 Obtener lista de actividades de otros usuarios
    @GET("api/tasks/others")
    Call<List<ActividadModel>> obtenerActividadesOtrosUsuarios(@Header("Authorization") String token);

    // 🔍 Buscar actividades por texto
    @GET("api/tasks/search")
    Call<List<ActividadModel>> searchTasks(
            @Header("Authorization") String token,
            @Query("texto") String textoBusqueda
    );

    // ✏️ Actualizar actividad
    @PUT("api/tasks/{id}")
    Call<ActividadModel> actualizarActividad(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body ActividadModel actividad
    );

    @DELETE("api/tasks/{id}")
    Call<Void> eliminarActividad(
            @Header("Authorization") String token,
            @Path("id") String id
    );


    // 📈 Obtener actividades promocionadas
    @GET("api/tasks/promoted")
    Call<List<ActividadModel>> getPromotedTasks();

    // 📣 Promover una actividad - PATCH
    @PATCH("api/tasks/{id}/promotion")
    Call<ResponseBody> promoteTask(
            @Path("id") String id,
            @Body PromotionRequest promotionRequest
    );

    // 🧑 Confirmar asistencia / Agregar asistente
    @POST("api/attendances/confirm")
    Call<Asistente> confirmAttendance(
            @Header("Authorization") String token,
            @Body Asistente asistente
    );

    // ❌ Cancelar asistencia a una actividad
    @DELETE("api/attendances/cancel/{taskId}")
    Call<Void> cancelAttendance(
            @Path("taskId") String taskId,
            @Header("Authorization") String token
    );

    // 📋 Obtener lista de asistentes por actividad
    @GET("api/attendances/{taskId}")
    Call<List<Asistente>> getAttendees(
            @Path("taskId") String taskId,
            @Header("Authorization") String token
    );

    // ✏️ Actualizar asistente
    @PUT("api/attendances/{id}")
    Call<Asistente> updateAttendance(
            @Path("id") String id,
            @Header("Authorization") String token,
            @Body Asistente asistente
    );

    // 🗑️ Eliminar asistente
    @DELETE("api/attendances/{id}")
    Call<Void> deleteAttendance(
            @Path("id") String id,
            @Header("Authorization") String token
    );
}