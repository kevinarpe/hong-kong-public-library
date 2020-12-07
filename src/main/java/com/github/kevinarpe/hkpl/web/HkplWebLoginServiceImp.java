package com.github.kevinarpe.hkpl.web;

import com.github.kklisura.cdt.protocol.events.page.LoadEventFired;
import com.github.kklisura.cdt.protocol.support.types.EventHandler;
import com.github.kklisura.cdt.protocol.types.page.Navigate;
import com.googlecode.kevinarpe.papaya.annotation.Blocking;
import com.googlecode.kevinarpe.papaya.annotation.OutputParam;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import com.googlecode.kevinarpe.papaya.function.count.ExactlyCountMatcher;
import com.googlecode.kevinarpe.papaya.function.retry.RetryService;
import com.googlecode.kevinarpe.papaya.function.retry.RetryStrategyFactory;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerLevel;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerService;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomNode;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomQuerySelectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.googlecode.kevinarpe.papaya.annotation.OutputParams.out;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class HkplWebLoginServiceImp
implements HkplWebLoginService {

    private static final Logger logger = LoggerFactory.getLogger(HkplWebLoginServiceImp.class);

    private final ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory;
    private final RetryService retryService;
    private final RetryStrategyFactory retryStrategyFactory;
    private final LoggerService loggerService;
    private final ExceptionThrower exceptionThrower;

    public HkplWebLoginServiceImp(ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory,
                                  RetryService retryService,
                                  RetryStrategyFactory retryStrategyFactory,
                                  LoggerService loggerService,
                                  ExceptionThrower exceptionThrower) {

        this.domQuerySelectorFactory =
            ObjectArgs.checkNotNull(domQuerySelectorFactory, "domQuerySelectorFactory");

        this.retryService = ObjectArgs.checkNotNull(retryService, "retryService");
        this.retryStrategyFactory = ObjectArgs.checkNotNull(retryStrategyFactory, "retryStrategyFactory");
        this.loggerService = ObjectArgs.checkNotNull(loggerService, "loggerService");
        this.exceptionThrower = ObjectArgs.checkNotNull(exceptionThrower, "exceptionThrower");
    }

    @Blocking
    @Override
    public void
    doLogin(Chrome chrome, HkplWebUserCredentials userCredentials)
    throws Exception {

        final boolean isFairLock = false;
        final Lock lock = new ReentrantLock(isFairLock);
        final Condition lockCondition = lock.newCondition();
        // @GuardedBy("lock")
        boolean[] isDoneRef = {false};
        final Exception[] exceptionRef = {null};

        chrome.chromeTab0.getPage().onLoadEventFired(new EventHandler<LoadEventFired>() {

            private int count = 0;

            @Override
            public void
            onEvent(LoadEventFired event) {

                ++count;
                loggerService.formatThenLog(logger, LoggerLevel.INFO,
                    "%s #%d", event.getClass().getSimpleName(), count);
                try {
                    switch (count) {

                        case 1: {
                            domQuerySelectorFactory.newInstance(chrome.chromeTab0)
                                .parentNodeIsDocument()
                                .expectedCount(new ExactlyCountMatcher(2))
                                .awaitQuerySelectorByIndexThenRun("#acc-box", 1, retryStrategyFactory,
                                    (ChromeDevToolsDomNode n) -> n.focus())
                                .sendKeys(userCredentials.username);

                            domQuerySelectorFactory.newInstance(chrome.chromeTab0)
                                .parentNodeIsDocument()
                                .expectedCount(new ExactlyCountMatcher(2))
                                .awaitQuerySelectorByIndexThenRun("#pass-box", 1, retryStrategyFactory,
                                    (ChromeDevToolsDomNode n) -> n.focus())
                                .sendKeys(userCredentials.password);

                            domQuerySelectorFactory.newInstance(chrome.chromeTab0)
                                .parentNodeIsDocument()
                                .expectedCount(new ExactlyCountMatcher(2))
                                .awaitQuerySelectorByIndex("a.ac_login_btn", 1, retryStrategyFactory)
                                .click();

                            _setDone(lock, out(isDoneRef), lockCondition);
                            break;
                        }
                        default: {
                            throw exceptionThrower.throwCheckedException(Exception.class,
                                "Internal error: Missing switch case %d", count);
                        }
                    }
                }
                catch (Exception e) {

                    exceptionRef[0] = e;
                    _setDone(lock, out(isDoneRef), lockCondition);
                }
            }
        });
        chrome.chromeTab0.getPage().enable();
/*
03:20:52.974 [main] DEBUG com.github.kklisura.cdt.services.impl.WebSocketServiceImpl -
Sending message {"id":2,"method":"Page.navigate","params":{"url":"https://www.hkpl.gov.hk/en/index.html"}}
on ws://localhost:41131/devtools/page/41B35E94A83C26C0EE9B577149C40392

03:20:52.977 [Grizzly(1)] DEBUG com.github.kklisura.cdt.services.impl.WebSocketServiceImpl -
Received message {"id":2,"result":{"frameId":"41B35E94A83C26C0EE9B577149C40392","loaderId":"8262DC29E712792B28036FB94BE8C017","errorText":"net::ERR_ABORTED"}}
on ws://localhost:41131/devtools/page/41B35E94A83C26C0EE9B577149C40392
 */
/* Good instance of Navigate:
errorText = null
frameId = "FFDF98E5B4E3A0C97382D7AE85BA3415"
loaderId = "7D5D2B614F86A4030F7F6E871369198D"
 */
        final Navigate navigate = retryService.call(retryStrategyFactory,
            () -> {
                final String url = "https://www.hkpl.gov.hk/en/index.html";
                final Navigate n = chrome.chromeTab0.getPage().navigate(url);
                @Nullable
                final String errorText = n.getErrorText();
                if (null != errorText) {
                    throw exceptionThrower.throwCheckedException(Exception.class,
                        "Failed to navigate to URL [%s]: [%s]", url, errorText);
                }
                return n;
            });

        _awaitDone(lock, lockCondition, isDoneRef);

        if (null != exceptionRef[0]) {
            throw exceptionRef[0];
        }

        chrome.chromeTab0.getPage().disable();
    }

    private void
    _setDone(Lock lock,
             @OutputParam boolean[] isDoneRef,
             Condition lockCondition) {

        lock.lock();
        isDoneRef[0] = true;
        try {
            lockCondition.signal();
        }
        finally {
            lock.unlock();
        }
    }

    private void
    _awaitDone(Lock lock,
               Condition lockCondition,
               boolean[] isDoneRef)
    throws Exception {

        lock.lock();
        try {
            while (false == isDoneRef[0]) {
                lockCondition.await();
            }
        }
        finally {
            lock.unlock();
        }
    }
}
