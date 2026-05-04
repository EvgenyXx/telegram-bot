package ru.pulsecore.app.modules.shared;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AdminProperties {

    private List<Long> superAdmins;
    private List<Long> admins;

    public boolean isAdmin(Long telegramId) {
        return admins != null && admins.contains(telegramId);
    }

    public boolean isSuperAdmin(Long id) {
        return superAdmins != null && superAdmins.contains(id);
    }


}