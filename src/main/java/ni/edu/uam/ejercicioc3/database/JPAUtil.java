package ni.edu.uam.ejercicioc3.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    // una sola fábrica para toda la aplicación (se crea una vez y se cierra al salir)
    private static EntityManagerFactory entityManagerFactory;

    public static synchronized void iniciar() {
        if (entityManagerFactory == null) {
            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isBlank()) {
                throw new RuntimeException("La variable de entorno DB_PASSWORD está vacía");
            }

            Map<String, Object> propiedades = new HashMap<>();
            propiedades.put("jakarta.persistence.jdbc.password", password);
            entityManagerFactory = Persistence.createEntityManagerFactory("laboratorioPU", propiedades);
        }
    }

    // un EntityManager nuevo por unidad de trabajo (cada DAO lo abre y lo cierra)
    public static EntityManager getEntityManager() {
        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}