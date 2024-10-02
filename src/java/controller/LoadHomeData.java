/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.JDBCType;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Manujaya
 */
@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("status", false);
        responseJson.addProperty("message", "Unable to process your request");

        try {

            Session session = HibernateUtil.getSessionFactory().openSession();

            //get user id from request parameter
            String userId = request.getParameter("id");

            //get user object
            User user = (User) session.get(User.class, Integer.parseInt(userId));

            //get user status = 1 (online)
            User_Status user_Status = (User_Status) session.get(User_Status.class, 1);

            //update user status
            user.setUser_Status(user_Status);
            session.update(user);

            //get other users
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.ne("id", user.getId()));

            List<User> otherUserList = criteria1.list();

            //remove passwords
            for (User otherUser : otherUserList) {
                otherUser.setPassword(null);
            }

            //get chat list
            Criteria criteria2 = session.createCriteria(Chat.class);
            criteria2.add(
                        Restrictions.or(
                                Restrictions.eq("from_user", user),
                                Restrictions.eq("to_user", user)
                        )
            );
            criteria2.addOrder(Order.desc("id"));
            
            //send users
            responseJson.addProperty("status", true);
            responseJson.addProperty("message", "Success");
            responseJson.addProperty("user", gson.toJson(user));
            responseJson.addProperty("otherUserList", gson.toJson(otherUserList));

            session.beginTransaction().commit();
            session.close();

        } catch (Exception e) {

        }
    }

}
