/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Manujaya
 */
@MultipartConfig
@WebServlet(name = "ChatSignUp", urlPatterns = {"/ChatSignUp"})
public class ChatSignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //එන content එක read කරනවා.
        Gson gson = new Gson(); //create gson lib
        JsonObject responseJson = new JsonObject(); //create java obj
        responseJson.addProperty("success", false);

//        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class); //req එකේ එන දේවල් JsonObject එකට map කරනවා
        String mobile = request.getParameter("mobile");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part avatarImage = request.getPart("avatarImage");

        if (mobile.isEmpty()) {
            //mobile number is empty
            responseJson.addProperty("message", "Please fill Mobile Number");
        } else if (!Validations.isMobileNumberValid(mobile)) {
            //invalid mobile number
            responseJson.addProperty("message", "Invalid Mobile Number");
        } else if (firstName.isEmpty()) {
            //firstName is empty
            responseJson.addProperty("message", "Please fill First Name");
        } else if (lastName.isEmpty()) {
            //lastName is empty
            responseJson.addProperty("message", "Please fill Last Name");
        } else if (password.isEmpty()) {
            //password is empty
            responseJson.addProperty("message", "Please fill Password");
        } else if (!Validations.isPasswordValid(password)) {
            //invalid password
            responseJson.addProperty("message", "Password  msut include at least one uppsercase letter, number, special character and be at least 8 character long");
        } else {
            System.out.println("HibernateUtil");
            Session session = HibernateUtil.getSessionFactory().openSession();

            //search mobile
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));

            if (!criteria1.list().isEmpty()) {
                //mobile number found
                responseJson.addProperty("message", "Mobile number already used");

            } else {
                //mobile number not used

                User user = new User();
                user.setFirst_name(firstName);
                user.setLast_name(lastName);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegistered_date_time(new Date());

                //get user status 2 = offline
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_Status(user_Status);

                session.save(user);
                
                System.out.println("Donee");
                session.beginTransaction().commit();

                //check uploaded image
                if (avatarImage != null) {
                    //image selected

                    String serverPath = request.getServletContext().getRealPath("");
                    String avatarImagePath = serverPath + File.separator + "AvatarImages" + File.separator + mobile + ".png";
                    System.out.println(avatarImagePath);
                    File file = new File(avatarImagePath);
                    Files.copy(avatarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Registration Complete");
            }

            session.close();

        }

//        System.out.println(request.getParameter("mobile"));
//        System.out.println(request.getParameter("firstName"));
//        System.out.println(request.getParameter("lastName"));
//        System.out.println(request.getParameter("password"));     
        //මේ පැත්තෙන් මොනා හරි යවන්න
//        responseJson.addProperty("message", "Server:Hello"); //set property into java obj/
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson)); //java obj --> json obj

    }

}
