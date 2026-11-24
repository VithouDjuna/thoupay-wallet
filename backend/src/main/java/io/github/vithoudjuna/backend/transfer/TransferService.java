package io.github.vithoudjuna.backend.transfer;

import io.github.vithoudjuna.backend.notification.internal.PushNotificationInternal;
import org.springframework.stereotype.Service;

@Service
public class TransferService {
    private final PushNotificationInternal pushNotificationInternal;

    public TransferService(PushNotificationInternal pushNotificationInternal) {
        this.pushNotificationInternal = pushNotificationInternal;
    }
}
