/*
 * Copyright 2011 Meyer Kizner
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prealpha.xylophone.server;

import java.lang.annotation.Annotation;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.internal.UniqueAnnotations;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.util.Providers;
import com.prealpha.xylophone.shared.Action;
import com.prealpha.xylophone.shared.Dispatcher;
import com.prealpha.xylophone.shared.PublishingDispatcher;

/**
 * Provides a {@link Dispatcher} and {@link PublishingDispatcher} implementation
 * which delegates action execution to a number of {@link ActionHandler}
 * implementations, one for each {@link Action} type. Handlers are designated
 * using the {@link #bindAction(Class)} method.
 * <p>
 * 
 * To use this module, create a subclass and override the abstract
 * {@link #configureActions()} method, which is intended for use in binding
 * handlers to specific action classes. See {@link #bindAction(Class)} for
 * details.
 * <p>
 * 
 * Once this module is installed, {@code Dispatcher},
 * {@code PublishingDispatcher}, and any {@code Action} classes configured are
 * considered bound by Guice. Further attempts to bind these types will result
 * in double binding exceptions at runtime. Additionally, the binding to the
 * {@code Action} interface itself should not be used.
 * 
 * @author Meyer Kizner
 * @see ActionHandler
 * 
 */
public abstract class ActionModule extends AbstractModule {
	/**
	 * Constructs a new {@code ActionModule}.
	 */
	protected ActionModule() {
	}

	/**
	 * Configures the underlying {@link Binder} by binding the
	 * {@link Dispatcher} and {@link PublishingDispatcher} interfaces to the
	 * internal implementation. The {@link #configureActions()} method is then
	 * called to allow for further configuration.
	 * 
	 * @see AbstractModule#configure()
	 */
	@Override
	protected final void configure() {
		bind(Dispatcher.class).to(PublishingDispatcher.class);
		bind(PublishingDispatcher.class).to(PublishingDispatcherImpl.class).in(
				Singleton.class);
		configureActions();
	}

	/**
	 * Provides the binding for {@link AsyncContext} which is required by
	 * {@link PublishingDispatcherImpl}.
	 * 
	 * @param request
	 *            the current servlet request
	 * @return an {@code AsyncContext} for the current request
	 */
	@Provides
	@RequestScoped
	@Inject
	AsyncContext getAsyncContext(HttpServletRequest request) {
		return request.startAsync();
	}

	/**
	 * Performs additional configuration by binding classes for dependency
	 * injection. While it is intended that the primary use of this method is
	 * through {@link #bindAction(Class)}, no real restriction is placed on the
	 * use of this method for other purposes. This may include any purpose for
	 * which {@link AbstractModule#configure()} method might be called.
	 * 
	 * @see #configure()
	 * @see #bindAction(Class)
	 */
	protected abstract void configureActions();

	/**
	 * Binds the handling of an {@link Action} class to a particular
	 * {@link ActionHandler} for the purposes of the {@link Dispatcher} provided
	 * by this module. An example might be: <blockquote>
	 * {@code bindAction(GetUser.class).to(GetUserHandler.class);} </blockquote>
	 * <p>
	 * 
	 * If an action handler is stateful, it should be bound in the singleton
	 * scope. Stateful action handlers are necessary when an action execution
	 * requires the use of partial results.
	 * <p>
	 * 
	 * When this method is called, the passed class is considered by Guice to be
	 * bound, just as if bound by some other method inherited from
	 * {@link AbstractModule}. Any attempt to re-bind the action class will
	 * result in a Guice provision exception at runtime, just like any other
	 * double binding.
	 * 
	 * @param actionClass
	 *            the action class to bind
	 * @return a {@link LinkedBindingBuilder} with which an action handler can
	 *         be bound to the action class
	 * @see ActionModule
	 */
	/*
	 * We create a unique annotation and use it to create a unique binding of
	 * Action to the target class. The returned binding builder binds handlers
	 * with the same annotation, so that the dispatcher can locate them using
	 * the annotation on the action class. Guice requires us to bind a provider
	 * for the action class itself, since it may not have a constructor which is
	 * eligible for JIT binding.
	 */
	/*
	 * Warnings are suppressed because Guice's DSL essentially ignores generics,
	 * so there would be no sense in attempting to return a
	 * LinkedBindingBuilder<ActionHandler<A, R>>. We could have a
	 * TypeLiteral-based implementation instead, but it's much more awkward to
	 * use and suffers from its own problems.
	 */
	@SuppressWarnings("rawtypes")
	protected final <A extends Action<?>> LinkedBindingBuilder<ActionHandler> bindAction(
			Class<A> actionClass) {
		Annotation annotation = UniqueAnnotations.create();
		bind(Action.class).annotatedWith(annotation).to(actionClass);
		bind(actionClass).toProvider(Providers.<A> of(null));
		return bind(ActionHandler.class).annotatedWith(annotation);
	}
}
