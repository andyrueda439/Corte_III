package ni.edu.uam.ejercicioc3.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import ni.edu.uam.ejercicioc3.database.JPAUtil;
import ni.edu.uam.ejercicioc3.model.Equipo;
import ni.edu.uam.ejercicioc3.model.TipoEquipo;

import java.util.List;

public class EquipoDAO {

    public List<Equipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "select e from Equipo e join fetch e.tipoEquipo order by e.idEquipo", Equipo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getIdTipo());
            if (tipo == null) {
                throw new RuntimeException("El tipo de equipo seleccionado no existe.");
            }
            equipo.setTipoEquipo(tipo);

            em.persist(equipo);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo existente = em.find(Equipo.class, equipo.getIdEquipo());
            if (existente == null) {
                tx.rollback();
                return false;
            }
            TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getIdTipo());
            if (tipo == null) {
                throw new RuntimeException("El tipo de equipo seleccionado no existe.");
            }
            equipo.setTipoEquipo(tipo);
            em.merge(equipo);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long idEquipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo equipo = em.find(Equipo.class, idEquipo);
            if (equipo == null) {
                tx.rollback();
                return false;
            }
            em.remove(equipo);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<Equipo> buscar(String nombre, TipoEquipo tipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            boolean hayNombre = nombre != null && !nombre.isBlank();
            boolean hayTipo = tipo != null;
            StringBuilder jpql = new StringBuilder("select e from Equipo e join fetch e.tipoEquipo t where 1 = 1");
            if (hayNombre) {
                jpql.append(" and lower(e.nombre) like :nombre");
            }
            if (hayTipo) {
                jpql.append(" and t.idTipo = :idTipo");
            }
            jpql.append(" order by e.idEquipo");

            TypedQuery<Equipo> consulta = em.createQuery(jpql.toString(), Equipo.class);
            if (hayNombre) {
                consulta.setParameter("nombre", "%" + nombre.trim().toLowerCase() + "%");
            }
            if (hayTipo) {
                consulta.setParameter("idTipo", tipo.getIdTipo());
            }
            return consulta.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existeCodigo(String codigo, Long idExcluir) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select count(e) from Equipo e where e.codigo = :codigo");
            if (idExcluir != null) {
                jpql.append(" and e.idEquipo <> :idExcluir");
            }

            TypedQuery<Long> consulta = em.createQuery(jpql.toString(), Long.class);
            consulta.setParameter("codigo", codigo);
            if (idExcluir != null) {
                consulta.setParameter("idExcluir", idExcluir);
            }
            return consulta.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}