package com.juan.consumo_movil.ui.gestionar;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.juan.consumo_movil.db.ManagerDb;
import com.juan.consumo_movil.models.Asistente;
import java.util.List;

public class GestionarViewModel extends AndroidViewModel {
    private ManagerDb managerDb;
    private MutableLiveData<List<Asistente>> asistentes;

    public GestionarViewModel(Application application) {
        super(application);
        managerDb = new ManagerDb(application);
        managerDb.open();
        asistentes = new MutableLiveData<>();
    }

    public LiveData<List<Asistente>> getAsistentes() {
        return asistentes;
    }

    public void cargarAsistentes(int idActividad) {
        List<Asistente> asistenteList = managerDb.obtenerAsistentesPorActividad(idActividad);
        asistentes.setValue(asistenteList);
    }

    public void insertarAsistente(Asistente asistente) {
        managerDb.insertarAsistente(asistente);
        actualizarAsistentes(asistente.getIdActividad());
    }

    public void actualizarAsistente(Asistente asistente) {
        managerDb.actualizarAsistente(asistente);
        actualizarAsistentes(asistente.getIdActividad());
    }

    public void eliminarAsistente(Asistente asistente) {
        managerDb.eliminarAsistente(asistente.getId());
        actualizarAsistentes(asistente.getIdActividad());
    }

    private void actualizarAsistentes(int idActividad) {
        List<Asistente> asistenteList = managerDb.obtenerAsistentesPorActividad(idActividad);
        asistentes.setValue(asistenteList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        managerDb.close();
    }
}