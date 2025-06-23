package com.juan.consumo_movil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabCreateActivity;
    private SessionManager sessionManager;

    // Para detectar doble clic en retroceso
    private boolean doubleBackToExitOnce = false;
    private Handler backPressedHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sessionManager = new SessionManager(this);

        // Si el usuario no está logueado, redirigir al login
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MenuActivity.this, InicioSesion.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inicializar vistas
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabCreateActivity = findViewById(R.id.fabCreateActivity);

        // Configurar FAB
        if (fabCreateActivity != null) {
            bottomNavigationView.post(() -> {
                int height = bottomNavigationView.getHeight();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fabCreateActivity.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, height + 80);
                fabCreateActivity.setLayoutParams(params);
                fabCreateActivity.setVisibility(View.VISIBLE);
            });

            fabCreateActivity.setOnClickListener(v -> {
                Intent intent = new Intent(MenuActivity.this, CrearActividad.class);
                startActivityForResult(intent, 1);
            });
        }

        // Cargar fragment inicial solo si es la primera vez
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

    // Método para cargar un fragmento en el contenedor
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Manejar resultado de actividad (ej: CrearActividad)
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

    // Doble clic en retroceso → Salir de la app
    @Override
    public void onBackPressed() {
        if (sessionManager.isLoggedIn()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                if (!doubleBackToExitOnce) {
                    doubleBackToExitOnce = true;
                    Toast.makeText(this, "Presiona nuevamente para salir", Toast.LENGTH_SHORT).show();
                    backPressedHandler.postDelayed(() -> doubleBackToExitOnce = false, 2000);
                } else {
                    // Limpiar sesión y cerrar app
                    sessionManager.cerrarSesion();

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        } else {
            super.onBackPressed();
        }
    }
}