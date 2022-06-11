package Communication.SesseionListener;

import Communication.Service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionTimeoutListener  implements ApplicationListener<SessionDestroyedEvent> {

    private final UserServiceImpl service;

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        List<SecurityContext> lstSecurityContext = event.getSecurityContexts();
        UserDetails user;
        for (SecurityContext securityContext : lstSecurityContext)
        {
            user = (UserDetails) securityContext.getAuthentication().getPrincipal();
            service.logout(user.getUsername());
        }
    }
}
