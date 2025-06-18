package com.juan.consumo_movil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.juan.consumo_movil.ui.actividades.FragmentActPanel;
import com.juan.consumo_movil.ui.comunidades.ComunidadesFragment;
import com.juan.consumo_movil.ui.perfil.PerfilFragment;
import com.juan.consumo_movil.ui.principal.PrincipalFragment;
import com.juan.consumo_movil.utils.SessionManager;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabCreateActivity;
    private SessionManager sessionManager;

    // Variable para manejar el doble clic
    private Handler backPressedHandler = new Handler();
    private Runnable backPressedRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sessionManager = new SessionManager(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabCreateActivity = findViewById(R.id.fabCreateActivity);

        // Verificar si el FAB existe
        if (fabCreateActivity != null) {
            Log.d(TAG, "FAB encontrado");

            // Configurar márgenes dinámicos según el BottomNavigationView
            bottomNavigationView.post(() -> {
                int height = bottomNavigationView.getHeight();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fabCreateActivity.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, height + 80); // margen adicional
                fabCreateActivity.setLayoutParams(params);
                fabCreateActivity.setVisibility(View.VISIBLE);
            });

            // Listener para abrir CrearActividad
            fabCreateActivity.setOnClickListener(v -> {
                Log.d(TAG, "FAB clickeado - Navegando a CrearActividad");
                Intent intent = new Intent(MenuActivity.this, CrearActividad.class);
                startActivityForResult(intent, 1);
            });
        } else {
            Log.e(TAG, "FAB NO encontrado. Revisa el ID en el XML.");
        }

        // Cargar fragmento principal si es primera vez
        if (savedInstanceState == null) {
            loadFragment(new PrincipalFragment());
        }

        // Configurar navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                fragment = new PrincipalFragment();
                fabCreateActivity.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.nav_actividades) {
                fragment = new FragmentActPanel();
                fabCreateActivity.setVisibility(View.GONE);
            } else if (itemId == R.id.nav_comunidades) {
                fragment = new ComunidadesFragment();
                fabCreateActivity.setVisibility(View.GONE);
            } else if (itemId == R.id.nav_perfil) {
                fragment = new PerfilFragment();
                fabCreateActivity.setVisibility(View.GONE);
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Agregar al stack para poder regresar
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof PrincipalFragment) {
                ((PrincipalFragment) currentFragment).cargarActividades();
            }
        }
    }

    // ⬇️ MÉTODO CLAVE PARA EVITAR SALIR CON UN SOLO RETROCESO
    @Override
    public void onBackPressed() {
        if (sessionManager.isLoggedIn()) {
            // Evitar que el usuario regrese a InicioSesion
            if (backPressedHandler.hasMessages(0)) {
                // Segundo clic rápido: Salir de la app
                backPressedHandler.removeMessages(0);
                finishAffinity(); // Cierra todas las actividades
                System.exit(0);
            } else {
                // Primer clic: Mostrar mensaje o ignorar
                Toast.makeText(this, "Presiona nuevamente para salir", Toast.LENGTH_SHORT).show();
                backPressedHandler.sendEmptyMessageDelayed(0, 2000); // Espera 2 segundos
            }
        } else {
            // Si no está logueado, comportamiento normal
            super.onBackPressed();
        }
    }
}