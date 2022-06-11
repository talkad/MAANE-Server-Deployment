package Communication.Resource;

import Communication.Security.KeyLoader;
import Domain.CommonClasses.Response;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionHandler {

    public Response<String> getUsernameByToken(String header){

        if(!header.startsWith("Bearer "))
            return new Response<>("", true, "username not found");

        String token = header.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256(KeyLoader.getInstance().getEncryptionKey("auth_key"));
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decodedJWT = verifier.verify(token);
        return new Response<>(decodedJWT.getSubject(), false, "username found");

    }
}
