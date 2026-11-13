package ni.edu.uam.ejercicioc3.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.ejercicioc3.database.JPAUtil;
import ni.edu.uam.ejercicioc3.model.TipoEquipo;

import java.util.List;

public class TipoEquipoDAO {

    public List<TipoEquipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select t from TipoEquipo t order by t.nombre", TipoEquipo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}