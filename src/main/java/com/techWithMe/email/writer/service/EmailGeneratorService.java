package com.techWithMe.email.writer.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techWithMe.email.writer.model.EmailRequest;

@Service
public class EmailGeneratorService {
	
	private final WebClient webClinet;
	
	@Value("${gemini.api.url}")
	private String geminiApiUrl;
	@Value("${gemini.api.key}")
	private String geminiApikey;
	
	public EmailGeneratorService(WebClient.Builder webClientBuilder) {
		this.webClinet = webClientBuilder.build();
	}

	public String generateEmailReply(EmailRequest emailRequest) {

		String prompt = buildPrompt(emailRequest);
		
		Map<String, Object> requestBody = Map.of(
				"contents", new Object[] {
						Map.of("parts" , new Object[] {
								Map.of("text",prompt) 
						})
				}
				);
		
		String response = webClinet.post()
				          .uri(geminiApiUrl + geminiApikey)
				          .header("Content-Type", "application/json")
				          .bodyValue(requestBody)
				          .retrieve()
				          .bodyToMono(String.class)
				          .block();
				
	      return extractResponseContent(response);
	}

	private String extractResponseContent(String response) {
		// TODO Auto-generated method stub
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(response);
			return rootNode.path("candidates").get(0)
					       .path("content")
					       .path("parts").get(0)
					       .path("text")
					       .asText();
		}catch(Exception e) {
			return "Error processing request" + e.getMessage();
		}
	
	}

	private String buildPrompt(EmailRequest emailRequest) {
		// TODO Auto-generated method stub
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate a Professional email reply for the following content. please  don't generate the subject line");
		if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
			prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
		}
		prompt.append("\nOriginal Email : \n").append(emailRequest.getEmailContent());
		return prompt.toString();
	}
}
