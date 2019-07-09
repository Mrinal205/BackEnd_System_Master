package com.moonassist.mailerlite.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonassist.mailerlite.bind.Group;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.util.List;

public class GroupsResponseHandler implements ResponseHandler<List<Group>> {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<Group> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

    int status = response.getStatusLine().getStatusCode();

    if (status > 300) {
      throw new ClientProtocolException("Unexpected response status: " + status);
    }

    HttpEntity entity = response.getEntity();
    return (entity != null) ? objectMapper.readValue(entity.getContent(), new TypeReference<List<Group>>(){}) : null;
  }
}
