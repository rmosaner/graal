/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.runtime;

import java.util.List;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.runtime.LLVMContext.ExternalLibrary;
import com.oracle.truffle.llvm.runtime.debug.scope.LLVMSourceLocation;
import com.oracle.truffle.llvm.runtime.memory.LLVMAllocateNode;
import com.oracle.truffle.llvm.runtime.memory.LLVMMemMoveNode;
import com.oracle.truffle.llvm.runtime.memory.LLVMMemSetNode;
import com.oracle.truffle.llvm.runtime.memory.LLVMMemoryOpNode;
import com.oracle.truffle.llvm.runtime.memory.LLVMStack.UniquesRegion;
import com.oracle.truffle.llvm.runtime.memory.LLVMStack.UniquesRegion.UniquesRegionAllocator;
import com.oracle.truffle.llvm.runtime.memory.VarargsAreaStackAllocationNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMControlFlowNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMExpressionNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMStatementNode;
import com.oracle.truffle.llvm.runtime.types.AggregateType;
import com.oracle.truffle.llvm.runtime.types.ArrayType;
import com.oracle.truffle.llvm.runtime.types.FunctionType;
import com.oracle.truffle.llvm.runtime.types.PrimitiveType.PrimitiveKind;
import com.oracle.truffle.llvm.runtime.types.StructureType;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.VectorType;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;

/**
 * This interface decouples the parser and the concrete implementation of the nodes by only making
 * {@link LLVMExpressionNode} and {@link LLVMExpressionNode} visible. The parser should not directly
 * instantiate a node, but instead use the factory facade.
 */
public interface NodeFactory {

    LLVMExpressionNode createInsertElement(Type resultType, LLVMExpressionNode vector, LLVMExpressionNode element, LLVMExpressionNode index);

    LLVMExpressionNode createExtractElement(Type resultType, LLVMExpressionNode vector, LLVMExpressionNode index);

    LLVMExpressionNode createShuffleVector(Type llvmType, LLVMExpressionNode vector1, LLVMExpressionNode vector2,
                    LLVMExpressionNode mask);

    LLVMStatementNode createStore(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWXchg(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWAdd(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWSub(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWAnd(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWNand(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWOr(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMExpressionNode createRMWXor(LLVMExpressionNode pointerNode, LLVMExpressionNode valueNode, Type type);

    LLVMStatementNode createFence();

    LLVMExpressionNode createArithmeticOp(ArithmeticOperation op, Type type, LLVMExpressionNode left, LLVMExpressionNode right);

    LLVMExpressionNode createLiteral(Object value, Type type);

    LLVMExpressionNode createSimpleConstantNoArray(Object constant, Type instructionType);

    LLVMExpressionNode createVectorLiteralNode(List<LLVMExpressionNode> listValues, Type type);

    LLVMStatementNode createFrameNuller(FrameSlot slot);

    LLVMControlFlowNode createRetVoid();

    LLVMControlFlowNode createNonVoidRet(LLVMExpressionNode retValue, Type resolvedType);

    LLVMExpressionNode createFunctionArgNode(int argIndex, Type paramType);

    LLVMControlFlowNode createFunctionInvoke(FrameSlot resultLocation, LLVMExpressionNode functionNode, LLVMExpressionNode[] argNodes, FunctionType type, int normalIndex,
                    int unwindIndex, LLVMStatementNode normalPhiWriteNodes,
                    LLVMStatementNode unwindPhiWriteNodes);

    LLVMStatementNode createFrameWrite(Type llvmType, LLVMExpressionNode result, FrameSlot slot);

    LLVMExpressionNode createComparison(CompareOperator operator, Type type, LLVMExpressionNode lhs, LLVMExpressionNode rhs);

    LLVMExpressionNode createSignedCast(LLVMExpressionNode fromNode, Type targetType);

    LLVMExpressionNode createSignedCast(LLVMExpressionNode fromNode, PrimitiveKind kind);

    LLVMExpressionNode createUnsignedCast(LLVMExpressionNode fromNode, Type targetType);

    LLVMExpressionNode createUnsignedCast(LLVMExpressionNode fromNode, PrimitiveKind kind);

    LLVMExpressionNode createBitcast(LLVMExpressionNode fromNode, Type targetType, Type fromType);

    LLVMExpressionNode createBitcast(LLVMExpressionNode fromNode, PrimitiveKind kind);

    LLVMExpressionNode createExtractValue(Type type, LLVMExpressionNode targetAddress);

    LLVMExpressionNode createTypedElementPointer(LLVMExpressionNode aggregateAddress, LLVMExpressionNode index, long indexedTypeLength,
                    Type targetType);

    LLVMExpressionNode createSelect(Type type, LLVMExpressionNode condition, LLVMExpressionNode trueValue, LLVMExpressionNode falseValue);

    LLVMExpressionNode createZeroVectorInitializer(int nrElements, VectorType llvmType);

    LLVMControlFlowNode createUnreachableNode();

    LLVMControlFlowNode createIndirectBranch(LLVMExpressionNode value, int[] labelTargets, LLVMStatementNode[] phiWrites);

    LLVMControlFlowNode createSwitch(LLVMExpressionNode cond, int[] labels, LLVMExpressionNode[] cases,
                    Type llvmType, LLVMStatementNode[] phiWriteNodes);

    LLVMControlFlowNode createConditionalBranch(int trueIndex, int falseIndex, LLVMExpressionNode conditionNode, LLVMStatementNode truePhiWriteNodes,
                    LLVMStatementNode falsePhiWriteNodes);

    LLVMControlFlowNode createUnconditionalBranch(int unconditionalIndex, LLVMStatementNode phi);

    LLVMExpressionNode createArrayLiteral(LLVMExpressionNode[] arrayValues, ArrayType arrayType, GetStackSpaceFactory arrayGetStackSpaceFactory);

    /*
     * Stack allocations with type
     */
    LLVMExpressionNode createAlloca(Type type);

    LLVMExpressionNode createAlloca(Type type, int alignment);

    LLVMExpressionNode createGetUniqueStackSpace(Type type, UniquesRegion uniquesRegion);

    LLVMExpressionNode createAllocaArray(Type elementType, LLVMExpressionNode numElements, int alignment);

    /*
     * Stack allocation without a type
     */
    VarargsAreaStackAllocationNode createVarargsAreaStackAllocation();

    LLVMExpressionNode createInsertValue(LLVMExpressionNode resultAggregate, LLVMExpressionNode sourceAggregate, int size, long offset, LLVMExpressionNode valueToInsert,
                    Type llvmType);

    LLVMExpressionNode createZeroNode(LLVMExpressionNode addressNode, int size);

    LLVMExpressionNode createStructureConstantNode(Type structureType, GetStackSpaceFactory getStackSpaceFactory, boolean packed, Type[] types, LLVMExpressionNode[] constants);

    LLVMExpressionNode createFunctionBlockNode(FrameSlot exceptionValueSlot, List<? extends LLVMStatementNode> basicBlockNodes, UniquesRegionAllocator uniquesRegionAllocator,
                    FrameSlot[][] beforeBlockNuller, FrameSlot[][] afterBlockNuller, LLVMStatementNode[] copyArgumentsToFrame, LLVMSourceLocation location, FrameDescriptor frameDescriptor, FrameSlot loopSuccessorSlot);

    RootNode createFunctionStartNode(LLVMExpressionNode functionBodyNode, FrameDescriptor frameDescriptor, String name, String originalName,
                    int argumentCount, Source bcSource, LLVMSourceLocation location);

    LLVMExpressionNode createInlineAssemblerExpression(ExternalLibrary library, String asmExpression, String asmFlags, LLVMExpressionNode[] args, Type[] argTypes, Type retType);

    LLVMExpressionNode createLandingPad(LLVMExpressionNode allocateLandingPadValue, FrameSlot exceptionSlot, boolean cleanup, long[] clauseKinds,
                    LLVMExpressionNode[] entries, LLVMExpressionNode getStack);

    LLVMControlFlowNode createResumeInstruction(FrameSlot exceptionSlot);

    LLVMExpressionNode createCompareExchangeInstruction(AggregateType returnType, Type elementType, LLVMExpressionNode ptrNode, LLVMExpressionNode cmpNode,
                    LLVMExpressionNode newNode);

    LLVMExpressionNode createLLVMBuiltin(Symbol target, LLVMExpressionNode[] args, Type[] argsTypes, int callerArgumentCount);

    LLVMStatementNode createPhi(LLVMExpressionNode[] from, FrameSlot[] to, Type[] types);

    LLVMExpressionNode createCopyStructByValue(Type type, GetStackSpaceFactory getStackSpaceFactory, LLVMExpressionNode parameterNode);

    LLVMExpressionNode createVarArgCompoundValue(int length, int alignment, LLVMExpressionNode parameterNode);

    LLVMMemMoveNode createMemMove();

    LLVMMemSetNode createMemSet();

    LLVMAllocateNode createAllocateGlobalsBlock(StructureType structType, boolean readOnly);

    LLVMMemoryOpNode createProtectGlobalsBlock();

    LLVMMemoryOpNode createFreeGlobalsBlock(boolean readOnly);

    LLVMExpressionNode createStackSave();

<<<<<<< HEAD
    LLVMExpressionNode createStackRestore(LLVMExpressionNode stackPointer);
=======
    LLVMObjectWriteNode createGlobalContainerWriteNode();

    LLVMControlFlowNode createLoop(LLVMExpressionNode body, int[] successorIDs);

    LLVMExpressionNode createLoopDispatchNode(FrameSlot exceptionValueSlot, List<? extends LLVMStatementNode> list, FrameSlot[][] beforeBlockNuller, FrameSlot[][] afterBlockNuller,
                    int headerId, int[] indexMapping, int[] successors, FrameSlot successorSlot);
>>>>>>> Integrate LLVMLoopNode into block dispatch.
}
