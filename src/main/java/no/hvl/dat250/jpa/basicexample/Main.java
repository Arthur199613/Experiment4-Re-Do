package no.hvl.dat250.jpa.basicexample;

import com.google.gson.Gson;

import java.util.List;

import javax.persistence.*;

import static spark.Spark.*;
import static spark.Spark.delete;

public class Main {
    private static final String PERSISTENCE_UNIT_NAME = "todos";
    private static EntityManagerFactory factory;

    public static void main(String[] args) {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();


        if (args.length > 0) {
            port(Integer.parseInt(args[0]));
        } else {
            port(8080);
        }



        after((req, res) -> {
            res.type("application/json");
        });

        get("/todos", (req, res) -> {
            Gson gson = new Gson();
            List<Todo> todos = em.createQuery("select t from Todo t").getResultList();
            return gson.toJson(todos);
        });


        post("/todos", (req, res) -> {
            Gson gson = new Gson();
            Todo todo = gson.fromJson(req.body(), Todo.class);

            em.getTransaction().begin();
            em.persist(todo);
            em.getTransaction().commit();
            return todo.toJson();
        });

        put("/todos/:id", (req, res) -> {
            Gson gson = new Gson();
            try {
                Todo todo = (Todo) em.createQuery("select t from Todo t where t.id=:id")
                        .setParameter("id", Long.parseLong(req.params(":id")))
                        .getSingleResult();

                Todo updatedTodo = gson.fromJson(req.body(), Todo.class);
                todo.setSummary(updatedTodo.getSummary());
                todo.setDescription(updatedTodo.getDescription());
                return todo.toJson();


            } catch (NoResultException e) {
                Todo todo = gson.fromJson(req.body(), Todo.class);
                em.getTransaction().begin();
                em.persist(todo);
                em.getTransaction().commit();
                return todo.toJson();
            }
        });

        delete("/todos/:id", (req, res) -> {
            try {
                Todo todo = (Todo) em.createQuery("select t from Todo t where t.id=:id")
                        .setParameter("id", Long.parseLong(req.params(":id")))
                        .getSingleResult();
                em.getTransaction().begin();
                em.remove(todo);
                em.getTransaction().commit();
                return ("Deleted todo " + req.params(":id"));

            } catch (NoResultException e) {
                return ("Couldn't find todo " + req.params(":id"));
            }
        });
    }
}
