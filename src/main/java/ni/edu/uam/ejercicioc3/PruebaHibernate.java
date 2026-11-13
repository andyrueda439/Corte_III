package ni.edu.uam.ejercicioc3;

import jakarta.persistence.EntityManager;
import ni.edu.uam.ejercicioc3.database.JPAUtil;
import ni.edu.uam.ejercicioc3.model.Equipo;

import java.util.List;

public class PruebaHibernate {
    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Equipo> equipos = em.createQuery(
                    "select e from Equipo e join fetch e.tipoEquipo order by e.idEquipo", Equipo.class
            ).getResultList();

            for (Equipo e : equipos) {
                System.out.println(e.getCodigo() + " | " + e.getNombre() + " | "
                        + e.getEstado() + " | " + e.getCantidad() + " | " + e.getTipoEquipo());
            }
        } finally {
            em.close();
            JPAUtil.cerrar();
        }
    }
}