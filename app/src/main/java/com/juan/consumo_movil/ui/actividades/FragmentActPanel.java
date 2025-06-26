package com.juan.consumo_movil.ui.actividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.ui.gestionar.GestionarFragment;
import com.juan.consumo_movil.ui.lista_actividades.ListaFragment;
import com.juan.consumo_movil.ui.promocionadas.PromocionadasFragment;
 // Asegúrate de que esta línea esté descomentada
import com.juan.consumo_movil.ui.recordatorio.FragmentRecordatorio;

public class FragmentActPanel extends Fragment {

    private CardView cardGestionAsistentes, cardListaActividades, cardPromocionadas, cardRecordatorio;

    public FragmentActPanel() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actipanel, container, false);

        cardGestionAsistentes = view.findViewById(R.id.card_gestion_asistentes);
        cardListaActividades = view.findViewById(R.id.card_lista_actividades);
        cardPromocionadas = view.findViewById(R.id.card_actividades_promocionadas);
        cardRecordatorio = view.findViewById(R.id.card_recordatorio);

        cardGestionAsistentes.setOnClickListener(v -> abrirFragmento(new GestionarFragment()));
        cardListaActividades.setOnClickListener(v -> abrirFragmento(new ListaFragment()));
        cardPromocionadas.setOnClickListener(v -> abrirFragmento(new PromocionadasFragment()));
        cardRecordatorio.setOnClickListener(v -> abrirFragmento(new FragmentRecordatorio())); // Línea activada

        aplicarEfectoPresion(view.findViewById(R.id.miBotton1));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton2));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton3));
        aplicarEfectoPresion(view.findViewById(R.id.miBotton4));

        return view;
    }

    private void abrirFragmento(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void aplicarEfectoPresion(View boton) {
        if (boton != null) {
            boton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        break;
                }
                return false;
            });
        }
    }
}