/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.mapmenus.script;

import com.google.common.base.Joiner;
import jdk.nashorn.api.scripting.JSObject;
import org.inventivetalent.scriptconfig.RuntimeScriptException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Scriptifier {

	public static <T> T scriptify(T source, Object target) {
		Class clazz = source.getClass();
		while (clazz != null) {
			scriptifyClass(clazz, source, target);
			clazz = clazz.getSuperclass();
		}
		return source;
	}

	private static void scriptifyClass(Class clazz, Object source, Object target) {
		for (Method method : clazz.getDeclaredMethods()) {
			Scriptify annotation = method.getAnnotation(Scriptify.class);
			if (annotation != null) {
				String name = annotation.name();
				String targetVar = annotation.targetVar();
				String[] params = annotation.params();

				if (name == null || name.isEmpty()) {
					name = method.getName();
				}
				if (params == null || params.length == 0) {
					Class[] parameterTypes = method.getParameterTypes();
					params = new String[parameterTypes.length];
					for (int i = 0; i < params.length; i++) {
						params[i] = "arg" + i;
					}
				}

				// Generate function
				String paramString = Joiner.on(",").join(params);
				if (target instanceof JSObject) {
					((JSObject) target).eval(String.format("this.%1$s = function(%2$s){this.%3$s.%1$s(%2$s);}", name, paramString, targetVar));
				} else if (target instanceof ScriptEngine) {
					try {
						((ScriptEngine) target).eval(String.format("this.%1$s = function(%2$s){this.%3$s.%1$s(%2$s);}", name, paramString, targetVar));
					} catch (ScriptException e) {
						throw new RuntimeScriptException(e);
					}
				} else {
					throw new IllegalArgumentException("cannot @Scriptifiy " + target.getClass());
				}
			}
		}
		for (Field field : clazz.getDeclaredFields()) {
			Scriptify annotation = field.getAnnotation(Scriptify.class);
			if (annotation != null) {
				String name = annotation.name();

				if (name == null || name.isEmpty()) {
					name = field.getName();
				}

				// Generate variable
				try {
					field.setAccessible(true);
					if (target instanceof JSObject) {
						((JSObject) target).setMember(name, field.get(source));
					} else if (target instanceof ScriptEngine) {
						((ScriptEngine) target).put(name, field.get(source));
					} else {
						throw new IllegalArgumentException("cannot @Scriptifiy " + (target==null?"null":target.getClass()));
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
