/**
 * 
 */
package at.dms.kjc.cluster;

import at.dms.kjc.CType;
import at.dms.kjc.common.CodegenPrintWriter;
import at.dms.kjc.KjcOptions;


/**
 * @author dimock
 *
 */
public abstract class TapeBase implements Tape {
    /** unique id of upstream node from NodeEnumerator */
    protected int src;
    /** unique id of downstream node from NodeEnumerator */
    protected int dst;
    /** type of items on the tape. */
    protected CType type;
    /** type of items on the tape as a string. */
    protected String typeString;
    
    /**
     * Constructor.
     * 
     * @param source  a number representing input operator
     * @param dest    a number representing output operator
     * @param type    a CType representing type of data communicated between the operators
     */
    public TapeBase(int source, int dest, CType type) {
        this.src = source;
        this.dst = dest;
        this.type = type;
        this.typeString = ClusterUtils.CTypeToString(type);
    }


    /**
     * Select type of tape and return one.
     * <br/>
     * Tapes connect nodes (filters, splitters, joiners).
     * An output of a node is the upstream end of a tape
     * found by {@link RegisterStreams#getNodeOutStreams(at.dms.kjc.sir.SIROperator)}.
     * An input of a node is the downstream end of a tape
     * found by {@link RegisterStreams#getNodeInStreams(at.dms.kjc.sir.SIROperator)}
     * @param src   Source node's integer id from NodeEnumerator.
     * @param dest  Destination Node's integer if deom NodeEnumerator.
     * @param type  Type of data to be passed on tape.
     * @return an object implementing Tape with sufficient info for code generation.
     * 
     */
    public static Tape newTape(int src, int dest, CType type) {
        Tape t = null;
        if (KjcOptions.standalone) {
            // if crosses between ssgs
            //   ...
            // else
            if (FixedBufferTape.needsModularBuffer(src,dest)) {
                t = new TapeFixedCircular(src,dest,type);
            } else {
                t = new TapeFixedCopydown(src,dest,type);
            }
        } else {
            if (ClusterFusion.fusedWith(NodeEnumerator.getFlatNode(src)).
                    contains(NodeEnumerator.getFlatNode(dest))) {
                t = new TapeClusterFused(src,dest,type);   
            } else {
                t = new TapeCluster(src,dest,type);
            }
        }
        return t;
    }


    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#getSource()
     */
    public int getSource() {
        return src;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#getDest()
     */
    public int getDest() {
        return dst;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#getType()
     */
    public CType getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#dataDeclarationH(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String dataDeclarationH();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#dataDeclaration(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String dataDeclaration();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#downstreamDeclarationExtern(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String downstreamDeclarationExtern();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#downstreamDeclaration(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String downstreamDeclaration();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#upstreamDeclarationExtern(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String upstreamDeclarationExtern();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#upstreamDeclaration(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String upstreamDeclaration();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#topOfWorkIteration(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String topOfWorkIteration();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#upstreamCleanup()
     */
    public abstract String upstreamCleanup();
    
    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#downstreamCleanup()
     */
    public abstract String downstreamCleanup();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushPrefix(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String pushPrefix();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushSuffix(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String pushSuffix();


    /* Override if can actually push many items at once.
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushManyItems(java.lang.String, int, int)
     */
    public String pushManyItems(String sourceBuffer, int sourceOffset, int numItems) {
        StringBuffer s = new StringBuffer();
        for (int y = 0; y < numItems; y++) {
            s.append(pushPrefix() + sourceBuffer + "[" + (sourceOffset+y) + "]" + pushSuffix() + ";");
        }
        return s.toString();
    }

    /* Override if can actually pop many items at once.
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#popManyItems(java.lang.String, int, int)
     */
    public String popManyItems(String destBuffer, int destOffset, int numItems) {
        StringBuffer s = new StringBuffer();
        for (int y = 0; y < numItems; y++) {
            s.append("    "+ destBuffer + "[" + (destOffset+y) + "] = " + popExpr() + ";\n");
        }
        return s.toString();
    }


    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#popExpr(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String popExpr();

    public abstract String popExprNoCleanup();
    
    public abstract String popExprCleanup();
    
   /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#popNPrefix(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String popNStmt(int N);

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#peekPrefix(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String peekPrefix();

    /* (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#peekSuffix(at.dms.kjc.common.CodegenPrintWriter)
     */
    public abstract String peekSuffix(); 
    /*
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushbackInit(int)
     */
    public abstract String pushbackInit(int NumberToPush);
    /*
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushbackPrefix()
     */
    public abstract String pushbackPrefix();
    /*
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushbackSuffix()
     */
    public abstract String pushbackSuffix();
    /*
     *  (non-Javadoc)
     * @see at.dms.kjc.cluster.Tape#pushbackCleanup()
     */
    public abstract String pushbackCleanup();

}