package com.juan.consumo_movil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.juan.consumo_movil.ui.actividades.FragmentActPanel;
import com.juan.consumo_movil.ui.comunidades.ComunidadesFragment;
import com.juan.consumo_movil.ui.perfil.PerfilFragment;
import com.juan.consumo_movil.ui.principal.PrincipalFragment;
public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabCreateActivity;
    private boolean isMenuEnabled = true; // Flag to prevent rapid taps
    private static final int MENU_TIMEOUT = 500; // Timeout in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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
            if (!isMenuEnabled) {
                return false; // Ignore taps if menu is disabled
            }

            isMenuEnabled = false; // Disable menu temporarily
            new Handler(Looper.getMainLooper()).postDelayed(() -> isMenuEnabled = true, MENU_TIMEOUT); // Re-enable menu after timeout

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
}