/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.javaagent;

import org.apache.sirona.configuration.Configuration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.Modifier;

import static java.lang.Integer.MIN_VALUE;

public class SironaClassVisitor
    extends ClassVisitor
    implements Opcodes
{

    public static final String TRACE_METHOD_PARAMETERS_KEY =
        Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.method.parameters.trace";

    private static final Boolean TRACE_METHOD_PARAMETERS = Configuration.is( TRACE_METHOD_PARAMETERS_KEY, false );

    private static final String STATIC_INIT = "<clinit>";

    private static final String CONSTRUCTOR = "<init>";

    private static final Type AGENT_CONTEXT = Type.getType( AgentContext.class );

    private static final Type STRING_TYPE = Type.getType( String.class );

    private static final Type OBJECT_TYPE = Type.getType( Object.class );

    private static final Type ARRAY_TYPE = Type.getType( Object[].class );

    private static final Type THROWABLE_TYPE = Type.getType( Throwable.class );

    private static final Type[] STOP_WITH_OBJECT_ARGS_TYPES = new Type[]{ OBJECT_TYPE };

    private static final Type[] STOP_WITH_THROWABLE_ARGS_TYPES = new Type[]{ THROWABLE_TYPE };

    private static final Type[] START_ARGS_TYPES = new Type[]{ OBJECT_TYPE, STRING_TYPE, ARRAY_TYPE };

    // methods
    public static final Method START_METHOD = new Method( "startOn", AGENT_CONTEXT, START_ARGS_TYPES );

    private static final Method STOP_METHOD = new Method( "stop", Type.VOID_TYPE, STOP_WITH_OBJECT_ARGS_TYPES );

    private static final Method STOP_WITH_EXCEPTION_METHOD =
        new Method( "stopWithException", Type.VOID_TYPE, STOP_WITH_THROWABLE_ARGS_TYPES );

    private final String javaName;

    private final byte[] classfileBuffer;

    private int count = 0;

    /**
     * @param writer
     * @param javaName
     * @param buffer   original class byte
     */
    public SironaClassVisitor( final ClassWriter writer, final String javaName, final byte[] buffer )
    {
        super( ASM5, writer );
        this.javaName = javaName;
        this.classfileBuffer = buffer;
    }

    @Override
    public void visitSource( final String source, final String debug )
    {
        super.visitSource( source, debug );
    }

    @Override
    public MethodVisitor visitMethod( final int access, final String name, final String desc, //
                                      final String signature, final String[] exceptions )
    {
        // final MethodVisitor visitor = new JSRInlinerAdapter(super.visitMethod(access, name, desc,
        // signature, exceptions), access, name, desc, signature, exceptions);
        final MethodVisitor visitor = super.visitMethod( access, name, desc, signature, exceptions );
        if ( !isSironable( access, name ) )
        {
            return visitor;
        }

        final String label = javaName.replace( "/", "." ) + "." //
            + name + "(" + typesToString( Type.getArgumentTypes( desc ) ) + ")";
        if ( AgentContext.listeners( label, classfileBuffer ) != null )
        {
            count++;
            return new SironaAdviceAdapter( visitor, access, name, desc, label );
        }
        return visitor;
    }

    private String typesToString( final Type[] argumentTypes )
    {
        final StringBuilder b = new StringBuilder();
        for ( final Type t : argumentTypes )
        {
            b.append( t.getClassName() ).append( "," );
        }
        if ( b.length() > 0 )
        {
            b.setLength( b.length() - 1 );
        }
        return b.toString();
    }

    protected boolean isSironable( final int access, final String name )
    {
        return !name.equals( STATIC_INIT ) //
            && !name.equals( CONSTRUCTOR ) //
            && !Modifier.isAbstract( access ) //
            && !Modifier.isNative( access );
    }

    public boolean wasAdviced()
    {
        return count > 0;
    }

    private class SironaAdviceAdapter
        extends AdviceAdapter
    {
        private final boolean isStatic;

        private final String label;

        private final String desc;

        public SironaAdviceAdapter( final MethodVisitor visitor, final int access, final String name, //
                                    final String desc, final String label )
        {
            super( ASM5, visitor, access, name, desc );
            this.isStatic = Modifier.isStatic( access );
            this.label = label;
            this.desc = desc;
        }

        private int ctxLocal;

        private final Label tryStart = new Label();

        private final Label endLabel = new Label();



        @Override
        public void onMethodEnter()
        {

            // we need to call static method startOn from AgentContext
            // startOn(final Object that, final String key, final Object[] methodParameters)

            if ( isStatic )
            {
                visitInsn( ACONST_NULL );
            }
            else
            {
                loadThis();
            }

            push( label );

            if ( TRACE_METHOD_PARAMETERS )
            {
                loadArgArray();
            }
            else
            {
                visitInsn( ACONST_NULL );
            }
            ctxLocal = newLocal( AGENT_CONTEXT );

            invokeStatic( AGENT_CONTEXT, START_METHOD );

            storeLocal( ctxLocal );

            visitLabel( tryStart );
        }




        @Override
        public void onMethodExit( final int opCode )
        {
            if ( opCode == ATHROW )
            {
                return;
            }

            int stateLocal = -1;
            if ( opCode != MIN_VALUE )
            {
                final Type returnType = Type.getReturnType( desc );
                final boolean isVoid = Type.VOID_TYPE.equals( returnType );
                if ( !isVoid )
                {
                    stateLocal = newLocal( returnType );
                    storeLocal( stateLocal );
                }
            }
            else
            {
                stateLocal = newLocal( THROWABLE_TYPE );
                storeLocal( stateLocal );
            }

            loadLocal( ctxLocal );
            if ( stateLocal != -1 )
            {
                loadLocal( stateLocal );
                if ( opCode != MIN_VALUE )
                {
                    valueOf( Type.getReturnType( desc ) );
                }
            }
            else
            {
                visitInsn( ACONST_NULL );
            }
            if ( opCode != MIN_VALUE )
            {
                invokeVirtual( AGENT_CONTEXT, STOP_METHOD );
            }
            else
            {
                invokeVirtual( AGENT_CONTEXT, STOP_WITH_EXCEPTION_METHOD );
            }

            if ( stateLocal != -1 )
            {
                loadLocal( stateLocal );
            }
        }

        @Override
        public void visitMaxs( final int maxStack, final int maxLocals )
        {
            visitLabel( endLabel );
            catchException( tryStart, endLabel, THROWABLE_TYPE );
            onMethodExit( MIN_VALUE );
            throwException();
            super.visitMaxs( 0, 0 );
        }


    }
}
