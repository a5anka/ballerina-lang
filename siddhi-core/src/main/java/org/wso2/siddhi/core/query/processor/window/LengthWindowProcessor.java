/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.event.EventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

public class LengthWindowProcessor extends WindowProcessor {

    private int length;
    private int count = 0;
    private EventChunk expiredEventChunk;
    private StreamEvent removeEventHead = null;
    private StreamEvent removeEventTail = null;
//    private StreamEventFactory removeEventFactory;

    @Override
    public void init(MetaStreamEvent metaStreamEvent) {
//        MetaStreamEvent   expiredMetaStreamEvent =  metaStreamEvent.clone();
        expiredEventChunk = new EventChunk(metaStreamEvent);
        if (parameters != null) {
            length = ((IntConstant) parameters[0]).getValue();
        }
    }

    @Override
    public void process(EventChunk eventChunk) {
//        expiredEventChunk.assign();
        while (eventChunk.hasNext()) {
            StreamEvent event = eventChunk.next();
            if (count > length) {

            }else {
//                expiredEventChunk.add();
              //  expiredEventChunk.add(event,true);
            }

        }
        StreamEvent event = eventChunk.getFirst();
        StreamEvent head = event;           //head of in events
        StreamEvent expiredEventTail;
        StreamEvent expiredEventHead;
        while (event != null) {
            processEvent(event);
            event = event.getNext();
        }
        //if window is expired
        if (count > length) {
            int diff = count - length;
            expiredEventTail = removeEventHead;
            for (int i = 1; i < diff; i++) {
                expiredEventTail = expiredEventTail.getNext();
            }
            expiredEventHead = removeEventHead;
            removeEventHead = expiredEventTail.getNext();
            expiredEventTail.setNext(null);
            addToLast(head, expiredEventHead);

            EventChunk headEventChunk = new EventChunk(null);
//            headEventChunk.setEventConverter(expiredEventChunk.getEventConverter());
            headEventChunk.assign(head);
            nextProcessor.process(headEventChunk);                            //emit in events and remove events as expired events
            count = count - diff;
        } else {
            EventChunk headEventChunk = new EventChunk(null);
//            headEventChunk.setEventConverter(expiredEventChunk.getEventConverter());
            headEventChunk.assign(head);
            nextProcessor.process(headEventChunk);                            //emit only in events as window is not expired
        }
    }

    private void addToLast(StreamEvent head, StreamEvent expiredEventHead) {
        StreamEvent last = head;
        while (null != last.getNext()) {
            last = last.getNext();
        }
        last.setNext(expiredEventHead);
    }

    /**
     * Create a copy of in event and store as remove event to emit at window expiration as expired events.
     *
     * @param event
     */
    private void processEvent(StreamEvent event) {
        StreamEvent removeEvent =null;//= removeEventFactory.newInstance();
        if (removeEvent.getOnAfterWindowData() != null) {
            System.arraycopy(event.getOnAfterWindowData(), 0, removeEvent.getOnAfterWindowData(), 0,
                    event.getOnAfterWindowData().length);
        }
        System.arraycopy(event.getOutputData(), 0, removeEvent.getOutputData(), 0, event.getOutputData().length);
        removeEvent.setExpired(true);
        if (removeEventHead == null) {      //better if we can do it in init()
            removeEventHead = removeEvent;
            removeEventTail = removeEvent;
        } else {
            removeEventTail.setNext(removeEvent);
            removeEventTail = removeEvent;
        }
        count++;
    }

    @Override
    public Processor cloneProcessor() {
        LengthWindowProcessor lengthWindowProcessor = new LengthWindowProcessor();
        lengthWindowProcessor.setLength(this.length);
        return lengthWindowProcessor;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
