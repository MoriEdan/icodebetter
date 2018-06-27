package iwb.controller;

import iwb.engine.FrameworkEngine;
import iwb.exception.PromisException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import iwb.util.FrameworkCache;
import iwb.util.FrameworkSetting;
import iwb.util.GenericUtil;
import iwb.util.HttpUtil;

@Controller
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private FrameworkEngine engine;
	
	private final String domain = FrameworkSetting.domain;
	private final String clientId = FrameworkSetting.clientId;
	private final String clientSecret = FrameworkSetting.clientSecret;
	
	private final AuthenticationController originController = AuthenticationController.newBuilder(domain, clientId, clientSecret).build();
    private final String userInfoAudience = String.format("https://%s/userinfo", domain);
    
    private final String redirectOnFail = "../auth/login";
    private final String redirectOnSuccess = "/iwb-lcp/app/main.htm";

   

    public Tokens handleRequest(HttpServletRequest request) throws IdentityVerificationException {
        return originController.handle(request);
    }

    public String buildAuthorizeUrl(HttpServletRequest request, String redirectUri) {
        return originController
                .buildAuthorizeUrl(request, redirectUri)
                .withAudience(userInfoAudience).withScope("openid profile email")
                .build();
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void getCallback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        handle(req, res);
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    protected void postCallback(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        handle(req, res);
    }

    private void handle(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Tokens tokens = handleRequest(req);
            String idToken = tokens.getIdToken();
            String issuer = "https://iwb.auth0.com/";
               
            Algorithm algorithm = Algorithm.HMAC256(clientSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build(); 
            DecodedJWT jwt = verifier.verify(idToken);
            
            Map<String, Claim> claims = jwt.getClaims();
            Claim fullNameClaim = claims.get("name");
            Claim nickClaim = claims.get("nickname");
            Claim eClaim = claims.get("email");
            Claim subClaim = claims.get("sub");
            Claim picClaim = claims.get("picture");
            
            String subject = subClaim.asString();
            int index = subject.indexOf("|");
            String fullName = fullNameClaim.asString();
            String socialNet = subject.substring(0, index);
            String email = eClaim.asString();
            String pictureUrl = picClaim.asString();
            String nickname = nickClaim.asString();
            
            int socialCon = 0;
            if(socialNet.equals("linkedin")){
            	socialCon = 1;
            }
            if(socialNet.equals("facebook")){
            	socialCon = 2;
            }
            if(socialNet.equals("google-oauth2")){
            	socialCon = 3;
            }
            
            HttpSession session = req.getSession(true);
            session.setAttribute("authToken", email);
            Map scd = engine.generateScdFromAuth(socialCon, email); 
            if(scd == null){
            	Map m = checkVcsTenant(socialCon, email,nickname,socialNet);
            	int customizationId = GenericUtil.uInt(m.get("customizationId"));
            	int userId = GenericUtil.uInt(m.get("userId"));

            	List<Map> projectList = (List<Map>)m.get("projects");
            	List<Map> userTips = (List<Map>)m.get("userTips");
            	//List<Map> userList = (List<Map>)m.get("userList");	
            	engine.saveCredentials(customizationId,userId,fullName, pictureUrl, socialCon, email, nickname, projectList, userTips);
            } else {
            	session.setAttribute("iwb-scd", scd);
            }
            res.sendRedirect(redirectOnSuccess);
        } catch (IdentityVerificationException e ) {
            e.printStackTrace();
            res.sendRedirect(redirectOnFail);
        } catch (JWTVerificationException exception) {
        	exception.printStackTrace();
        	res.sendRedirect(redirectOnFail);
        }
        
    }
    
    private Map checkVcsTenant(int socialCon, String email, String nickname, String socialNet) {
		String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url");
		try {
			JSONObject params = new JSONObject(); 
			params.put("email", email);
			params.put("socialCon", socialCon);
			params.put("nickname", nickname);
			params.put("socialNet", socialNet);
	    	String s = HttpUtil.sendJson(vcsUrl+"serverVCSTenantCheck", params);
			if(!GenericUtil.isEmpty(s)){
				JSONObject json;
				try {
					json = new JSONObject(s);
					if(json.get("success").toString().equals("true")){
						Map map = GenericUtil.fromJSONObjectToMap(json);
						/*Map map = new HashMap();
					    map.put("customizationId", json.get("customizationId"));
					    map.put("projectId", json.get("projectId").toString());
					    map.put("userId", json.get("userId"));
					    map.put("userTip", json.get("userTip"));
					    */
						return map;
					}
				} catch (JSONException e){
					//throw new PromisException("vcs","vcsClientObjectPush:JSON Exception", t.getTableId(), s, e.getMessage(), e.getCause());
				}
			}
		}catch (JSONException e){
			throw new RuntimeException();
		}
		return null;
	}

	@RequestMapping("/login")
    @ResponseBody
    protected void login(final HttpServletRequest req, HttpServletResponse res) throws IOException {
        String redirectUri = req.getScheme() +"://localhost:8080/iwb-lcp/auth/callback";// "promiscrm.com:8888" + "/iwb/auth/callback"; //req.getScheme() + "://localhost:9999/iwb-lcp/auth/callback";
        String authorizeUrl = buildAuthorizeUrl(req, redirectUri);
    	res.getWriter().write(authorizeUrl);
		res.getWriter().close();
        //return "redirect:" + authorizeUrl;
    }
    
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    protected String logout(final HttpServletRequest req) {
        
        invalidateSession(req);
        String returnTo = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        String logoutUrl = String.format("https://%s/v2/logout?client_id=%s&returnTo=%s", domain, clientId, returnTo);
        return "redirect:" + logoutUrl;
    }

    private void invalidateSession(HttpServletRequest request) {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
    }
    
    @RequestMapping("/redirectAuth")
    private void authRedirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
    	res.sendRedirect(redirectOnFail);
    }
    

}