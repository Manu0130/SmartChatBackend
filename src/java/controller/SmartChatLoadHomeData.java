/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.JDBCType;
import java.text.SimpleDateFormat;
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
@WebServlet(name = "SmartChatLoadHomeData", urlPatterns = {"/SmartChatLoadHomeData"})
public class SmartChatLoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);
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

            //get other user one by one
            JsonArray jsonChatArray = new JsonArray();
            
            for (User otherUser : otherUserList) {

                //get last conversation
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq("from_user", user),
                                        Restrictions.eq("to_user", otherUser)
                                ),
                                Restrictions.and(
                                        Restrictions.eq("from_user", otherUser),
                                        Restrictions.eq("to_user", user)
                                )
                        )
                );
                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(1);

                //create chat item json to send frontend data
                JsonObject jsonChatItem = new JsonObject();
                jsonChatItem.addProperty("other_user_id", otherUser.getId());
                jsonChatItem.addProperty("other_user_mobile", otherUser.getMobile());
                jsonChatItem.addProperty("other_user_name", otherUser.getFirst_name() + " " + otherUser.getLast_name());
                jsonChatItem.addProperty("other_user_status", otherUser.getUser_Status().getId()); ////1 = online, 2 = offline

                //check avatar image
                String serverPath = request.getServletContext().getRealPath("");
                String otherUserAvatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + otherUser.getMobile() + ".png";
                File otherUserAvatarImageFile = new File(otherUserAvatarImagePath);

                if (otherUserAvatarImageFile.exists()) {
                    //avatar image found
                    jsonChatItem.addProperty("avatar_image_found", true);

                } else {
                    //avatar image not found
                    jsonChatItem.addProperty("avatar_image_found", false);
                    jsonChatItem.addProperty("other_user_avatar_letters", otherUser.getFirst_name().charAt(0) + "" + otherUser.getLast_name().charAt(0));

                }

                //get chat list
                List<Chat> dbChatList = criteria2.list();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MM dd hh:ss a");

                if (dbChatList.isEmpty()) {
                    //no chat
                    jsonChatItem.addProperty("message", "Let's Start New Conversation");
                    jsonChatItem.addProperty("dateTime", dateFormat.format(user.getRegistered_date_time()));
                    jsonChatItem.addProperty("chat_status_id", 1); //1 = seen, 2 = unseen
                } else {
                    //found last chat
                    jsonChatItem.addProperty("message", dbChatList.get(0).getMessage());
                    jsonChatItem.addProperty("dateTime", dateFormat.format(dbChatList.get(0).getDate_time()));
                    jsonChatItem.addProperty("chat_status_id", dbChatList.get(0).getChat_Status().getId());

                }
                //get last conversation
                jsonChatArray.add(jsonChatItem);
//                otherUser.setPassword(null);
            }

            //send users
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Success");
//            responseJson.add("user", gson.toJsonTree(user)); //user ව යවන්නෙ නැති නිසා
            responseJson.add("jsonChatArray", gson.toJsonTree(jsonChatArray));
//            response.getWriter().write(jsonChatArray);
            
            session.beginTransaction().commit();
            session.close();

        } catch (Exception e) {

        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }

}
