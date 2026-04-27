package top.emilejones.hhu.application.platform.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.common.exception.ConflictException;

@Component
public class PipelineStateMachineListener extends StateMachineListenerAdapter<PipelineState, PipelineEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PipelineStateMachineListener.class);

    @Override
    public void eventNotAccepted(Message<PipelineEvent> event) {
        logger.error("Pipeline state machine event not accepted. Event: {}", event);
        throw new ConflictException("当前状态无法接受事件: " + event.getPayload());
    }
}
