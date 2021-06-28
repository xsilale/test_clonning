package com.example.MyBookShopApp.security;

import com.example.MyBookShopApp.data.model.SmsCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthUserController {

    private final BookstoreUserRegister userRegister;
    private final SmsService smsService;
    private final JavaMailSender javaMailSender;

    @Autowired
    public AuthUserController(BookstoreUserRegister userRegister, SmsService smsService, JavaMailSender javaMailSender) {
        this.userRegister = userRegister;
        this.smsService = smsService;
        this.javaMailSender = javaMailSender;
    }

    @Value("${appEmail.email}")
    private String email;

    @GetMapping("/signin")
    public String handleSignIn() {
        return "signin";
    }

    @GetMapping("/signup")
    public String handleSignUp(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        return "signup";
    }

    @PostMapping("/requestContactConfirmation")
    @ResponseBody
    public ContactConfirmationResponse handleRequestContactConfirmation(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        if (payload.getContact().contains("@")) {
            return response; //for email
        } else {
            String smsCodeString = smsService.sendSecretCodeSms(payload.getContact());
            smsService.saveNewCode(new SmsCode(smsCodeString, 60)); //expires in 1 min.
            return response;
        }
    }

    @PostMapping("/requestEmailConfirmation")
    @ResponseBody
    public ContactConfirmationResponse handleRequestEmailConfirmation(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(payload.getContact());
        SmsCode smsCode = new SmsCode(smsService.generateCode(), 300); //5 minutes
        smsService.saveNewCode(smsCode);
        message.setSubject("Bookstore email verification!");
        message.setText("Verification code is: " + smsCode.getCode());
        javaMailSender.send(message);
        response.setResult("true");
        return response;
    }

    @PostMapping("/approveContact")
    @ResponseBody
    public ContactConfirmationResponse handleApproveContact(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();

        if (smsService.verifyCode(payload.getCode())) {
            response.setResult("true");
        }
        return response;
    }

    @PostMapping("/reg")
    public String handleUserRegistration(RegistrationForm registrationForm, Model model) {
        userRegister.registerNewUser(registrationForm);
        model.addAttribute("regOK", true);
        return "signin";
    }

    @PostMapping("/login")
    @ResponseBody
    public ContactConfirmationResponse handleLogin(@CookieValue(value = "cartContents", required = false) String cartContents,
                                                   @RequestBody ContactConfirmationPayload payload,
                                                   HttpServletResponse httpServletResponse) {
        ContactConfirmationResponse loginResponse = userRegister.jwtLogin(payload);
        Cookie cookie = new Cookie("token", loginResponse.getResult());
        httpServletResponse.addCookie(cookie);
        if (cartContents != null) {
            Cookie cookieContent = new Cookie("cartContents", cartContents);
            httpServletResponse.addCookie(cookieContent);
        }
        return loginResponse;
    }

    @PostMapping("/login-by-phone-number")
    @ResponseBody
    public ContactConfirmationResponse handleLoginByPhoneNumber(@RequestBody ContactConfirmationPayload payload,
                                                   HttpServletResponse httpServletResponse) {
        if (smsService.verifyCode(payload.getCode())) {
            ContactConfirmationResponse loginResponse = userRegister.jwtLoginByPhoneNumber(payload);
            Cookie cookie = new Cookie("token", loginResponse.getResult());
            httpServletResponse.addCookie(cookie);
            return loginResponse;
        } else {
            return null;
        }
    }

    @GetMapping("/my")
    public String handleMy(Model model) {
        model.addAttribute("curUsr", userRegister.getCurrentUser());
        return "my";
    }

//    @GetMapping("/logout")
//    public String handleLogout(HttpServletRequest request) {
//        HttpSession session = request.getSession();
//        SecurityContextHolder.clearContext();
//        if (session != null) {
//            session.invalidate();
//        }
//
//        for (Cookie cookie : request.getCookies()) {
//            cookie.setMaxAge(0);
//        }
//        return "redirect:/";
//    }
}
