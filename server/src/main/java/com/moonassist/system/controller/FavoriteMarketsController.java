package com.moonassist.system.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.WebUtils;

import com.moonassist.bind.account.FavoriteMarket;
import com.moonassist.service.AuthenticationService;
import com.moonassist.service.FavoriteMarketsService;
import com.moonassist.system.security.SecurityConstants;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/favorites")
public class FavoriteMarketsController extends BaseController {

    @Autowired
    private FavoriteMarketsService favoriteMarketsService;
    @Autowired
    private AuthenticationService authenticationService;

    @ResponseBody
    @RequestMapping(value = "/markets", method = RequestMethod.GET)
    public List<FavoriteMarket> get(HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException, IOException {

        String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
        Id<UserId> userId = authenticationService.retrieveAuthentication(token);

        return favoriteMarketsService.all(userId);
    }

    @ResponseBody
    @RequestMapping(value = "/markets", method = RequestMethod.POST)
    public ResponseEntity<FavoriteMarket> create(@RequestBody FavoriteMarket favoriteMarket, HttpServletRequest httpServletRequest)
            throws AuthenticationService.AuthenticationException {
        favoriteMarket.validate();
        String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
        Id<UserId> userId = authenticationService.retrieveAuthentication(token);

        FavoriteMarket createdFavoriteMarket = favoriteMarketsService.save(favoriteMarket);
        return new ResponseEntity<>(createdFavoriteMarket, HttpStatus.OK);
    }

    @RequestMapping(value = "/markets/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") String id, HttpServletRequest httpServletRequest)
            throws AuthenticationService.AuthenticationException, IOException {

        String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
        Id<UserId> userId = authenticationService.retrieveAuthentication(token);
        favoriteMarketsService.delete(Id.from(id));
    }

}