package com.tcfuture.akka.actor.interactionpatterns.future;

import akka.Done;

import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 */
public interface CustomerDataAccess {
    CompletionStage<Done> update(Customer customer);
}
