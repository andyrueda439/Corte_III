package ni.edu.uam.ejercicioc3;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ni.edu.uam.ejercicioc3.database.JPAUtil;
import ni.edu.uam.ejercicioc3.model.Equipo;
import ni.edu.uam.ejercicioc3.model.TipoEquipo;

public class PruebaRollback {

    private static long contarEquipos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select count(e) from Equipo e", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    private static long contarPorCodigo(String codigo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select count(e) from Equipo e where e.codigo = :codigo", Long.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) {
        long antes = contarEquipos();
        System.out.println("Equipos antes de la transacción: " + antes);

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TipoEquipo tipo = em.createQuery("select t from TipoEquipo t order by t.idTipo", TipoEquipo.class)
                    .setMaxResults(1)
                    .getSingleResult();

            em.persist(new Equipo("EQ-RB1", "Equipo de prueba rollback", "Prueba", "Disponible", 1, tipo));
            System.out.println("Se hizo persist() del equipo válido EQ-RB1 (sin commit todavía)");

            em.persist(new Equipo("EQ-001", "Equipo con código repetido", "Prueba", "Disponible", 1, tipo));

            tx.commit();
            System.out.println("commit() realizado (NO debería llegar aquí)");
        } catch (RuntimeException ex) {
            Throwable causa = ex;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            System.out.println("FALLO por restricción de integridad: " + causa.getMessage());

            if (tx.isActive()) {
                tx.rollback();
                System.out.println("rollback() ejecutado: se deshace TODA la transacción");
            }
        } finally {
            em.close();
        }

        long despues = contarEquipos();
        System.out.println("Equipos DESPUÉS de la transacción: " + despues);
        System.out.println("¿Existe EQ-RB1 (la escritura válida)? filas = " + contarPorCodigo("EQ-RB1"));
        System.out.println(antes == despues ? "OK: no quedaron cambios parciales" : "ERROR: quedaron cambios parciales");

        JPAUtil.cerrar();
    }
}