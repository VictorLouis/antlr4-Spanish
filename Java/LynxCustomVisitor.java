package unal.lenguajes.parcialfinal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.antlr.v4.runtime.tree.TerminalNode;

import unal.lenguajes.parcialfinal.LynxParser.FuncionXContext;

public class LynxCustomVisitor extends LynxBaseVisitor<Value> {
    
    // usado para comparar lo numeros flotantes
    public static final double TINY_FLOAT = 0.00000000001;

    // store variables (there's only one global scope!)
    private Map<String, Value> memory = new HashMap<String, Value>();
    
    // store functions (there's only one global scope!)
    private Map<String, FuncionXContext> funcmemo = new HashMap<String, FuncionXContext>();
    
    // store structures <struct name, attributes>(there's only one global scope!)
    private Map<String, List<String>> structmemo = new HashMap<String, List<String>>();
    
    // store structures variables <variable of structure name, type>(there's only one global scope!)
    private Map<String, String> structvars = new HashMap<String, String>();
    
    @Override 
    public Value visitDeclaracion(LynxParser.DeclaracionContext ctx) {
        String id = ctx.ID().getText();
        return memory.put(id, null); 
    }
    
    @Override 
    public Value visitAsignador(LynxParser.AsignadorContext ctx) { 
        String id = ctx.ID().getText();
        Value value = this.visit(ctx.expresion());
        return memory.put(id, value); 
    }
    
    @Override
    public Value visitFuncionSistema(LynxParser.FuncionSistemaContext ctx) {
        Value value = this.visit(ctx.expresion());
        System.out.println(value);
        return value;
    }
    
    @Override
    public Value visitFuncionLeer(LynxParser.FuncionLeerContext ctx) { 
    	String var = ctx.ID().getText();
    	@SuppressWarnings("resource")
		Scanner user_input = new Scanner( System.in );
    	String type = ctx.TYPES().getText();
    	Value varval = null;
    	
    	if(type.equals("cadena")){
	    	String value = user_input.nextLine();
	    	varval = new Value(value);
    	}else if(type.equals("real")){
    		String value = user_input.next( );
    		double vv = Double.parseDouble(value);
    		varval = new Value(vv);
    	}else{
    		String value = user_input.next( );
    		int vv = Integer.parseInt(value);
    		varval = new Value(vv);
    	}
    	return memory.put(var, varval);
    }

    // atomics overrides
    @Override 
    public Value visitParExpr(LynxParser.ParExprContext ctx) { 
        return this.visit(ctx.expresion());
    }

    @Override 
    public Value visitIntegerAtom(LynxParser.IntegerAtomContext ctx) {
        return new Value(Integer.valueOf(ctx.getText()));
    }

    @Override 
    public Value visitFloatAtom(LynxParser.FloatAtomContext ctx) {
        return new Value(Double.valueOf(ctx.getText()));
    }

    @Override 
    public Value visitBooleanAtom(LynxParser.BooleanAtomContext ctx) {
        return new Value(Boolean.valueOf(ctx.getText()));
    }

    @Override
    public Value visitIdAtom(LynxParser.IdAtomContext ctx) {
        String id = ctx.getText();
        Value value = memory.get(id);
        if(value == null) {
            throw new RuntimeException("Esta variable no existe: " + id);
        }
        return value;
    }

    @Override 
    public Value visitCharAtom(LynxParser.CharAtomContext ctx) {
        return new Value(String.valueOf(ctx.getText()));
    }

    @Override
    public Value visitStringAtom(LynxParser.StringAtomContext ctx) {
        String str = ctx.getText();
        // strip quotes
        str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
        return new Value(str);
    }

    @Override
    public Value visitNullAtom(LynxParser.NullAtomContext ctx) {
        return new Value(null);
    }

    // expresion overrides
     @Override
    public Value visitUnaryMinusExpr(LynxParser.UnaryMinusExprContext ctx) {
        Value value = this.visit(ctx.expresion());
        return new Value(-value.asDouble());
    }

    @Override
    public Value visitNotExpr(LynxParser.NotExprContext ctx) {
        Value value = this.visit(ctx.expresion());
        return new Value(!value.asBoolean());
    }

    @Override
    public Value visitMultiplicationExpr(LynxParser.MultiplicationExprContext ctx) {

        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));

        switch (ctx.op.getType()) {
            case LynxParser.MULT:
                return new Value(left.asDouble() * right.asDouble());
            case LynxParser.DIV:
                return new Value(left.asDouble() / right.asDouble());
            case LynxParser.MOD:
                return new Value(left.asDouble() % right.asDouble());
            default:
                throw new RuntimeException("Operador desconocido: " + ctx.op.getType());
        }
    }

    @Override
    public Value visitAdditiveExpr(LynxParser.AdditiveExprContext ctx) {

        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));

        switch (ctx.op.getType()) {
            case LynxParser.PLUS:
                return left.isDouble() && right.isDouble() ?
                        new Value(left.asDouble() + right.asDouble()) :
                        new Value(left.asString() + right.asString());
            case LynxParser.MINUS:
                return new Value(left.asDouble() - right.asDouble());
            default:
                throw new RuntimeException("Operador desconocido: " + ctx.op.getType());
        }
    }

    @Override
    public Value visitRelationalExpr(LynxParser.RelationalExprContext ctx) {

        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));

        switch (ctx.op.getType()) {
            case LynxParser.LT:
                return new Value(left.asDouble() < right.asDouble());
            case LynxParser.LTEQ:
                return new Value(left.asDouble() <= right.asDouble());
            case LynxParser.GT:
                return new Value(left.asDouble() > right.asDouble());
            case LynxParser.GTEQ:
                return new Value(left.asDouble() >= right.asDouble());
            default:
                throw new RuntimeException("Operador desconocido: " + ctx.op.getType());
        }
    }

    @Override
    public Value visitEqualityExpr(LynxParser.EqualityExprContext ctx) {

        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));

        switch (ctx.op.getType()) {
            case LynxParser.EQUALITY:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asDouble() - right.asDouble()) < TINY_FLOAT) :
                        new Value(left.equals(right));
            case LynxParser.NOTEQUAL:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asDouble() - right.asDouble()) >= TINY_FLOAT) :
                        new Value(!left.equals(right));
            default:
                throw new RuntimeException("Operador desconocido: " + ctx.op.getType());
        }
    }
    
    @Override
    public Value visitTrigExpr(LynxParser.TrigExprContext ctx){
        Value value=this.visit(ctx.expresion());
        
        switch (ctx.opt.getType()) {
            case LynxParser.SEN:
                return new Value(Math.sin(value.asDouble()));

            case LynxParser.COS:
                return new Value(Math.cos(value.asDouble())); 
                
            case LynxParser.TAN:
                return new Value(Math.tan(value.asDouble()));
                
            case LynxParser.SENH:
                return new Value(Math.sinh(value.asDouble()));

            case LynxParser.COSH:
                return new Value(Math.cosh(value.asDouble())); 
                
            case LynxParser.TANH:
                return new Value(Math.tanh(value.asDouble()));
                
            default:    
                throw new RuntimeException("Funcion desconocida: " + ctx.opt.getType());
        }
    }
    
    @Override
    public Value visitTwoparamExpr(LynxParser.TwoparamExprContext ctx) { 
    	Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));
    	
        switch (ctx.opt.getType()) {
        case LynxParser.MIN:
            return new Value(Math.min(left.asDouble(),right.asDouble()));

        case LynxParser.MAX:
            return new Value(Math.max(left.asDouble(),right.asDouble()));
            
        default:    
        	throw new RuntimeException("Funcion desconocida: " + ctx.opt.getType());
        }
    }
    
    @Override 
    public Value visitMathfuncExpr(LynxParser.MathfuncExprContext ctx) { 
    	Value value=this.visit(ctx.expresion());
        
        switch (ctx.opt.getType()) {
            case LynxParser.CEIL:
                return new Value(Math.ceil(value.asDouble()));

            case LynxParser.FLOOR:
                return new Value(Math.floor(value.asDouble())); 
                
            case LynxParser.SQRT:
                return new Value(Math.sqrt(value.asDouble()));
                
            case LynxParser.ABS:
                return new Value(Math.abs(value.asDouble()));

            case LynxParser.LOG:
                return new Value(Math.log(value.asDouble())); 
                
            case LynxParser.LOG10:
                return new Value(Math.log10(value.asDouble()));
                
            default:    
                throw new RuntimeException("Funcion desconocida: " + ctx.opt.getType());
        }
    }
    
    @Override 
    public Value visitCallfuncExpr(LynxParser.CallfuncExprContext ctx) { 
    	String name = ctx.ID().getText();
    	FuncionXContext fctx = funcmemo.get(name);
    	
    	if(!funcmemo.containsKey(name)) {
            throw new RuntimeException("Esta funcion no existe: " + name);
        }
    	List<TerminalNode> ids = fctx.parametros().ID();
    	List<LynxParser.ExpresionContext> callp = ctx.parametrosCall().expresion();
    	
    	if( ids.size() != callp.size()){
    		throw new RuntimeException("Los parametros no coinciden: " + name);
    	}
    	
    	List<TerminalNode> types = fctx.parametros().TYPES();
    	
        for(int i = 0; i < ids.size(); i++){
	        	if(types.get(i).getText().equals("real")){
	        		Value parav = new Value(this.visit(callp.get(i)).asDouble());
	        		memory.put(ids.get(i).getText(), parav);
	        	}
	        	else if(types.get(i).getText().equals("cadena")){
	        		Value parav = new Value(this.visit(callp.get(i)).asString());
	        		memory.put(ids.get(i).getText(), parav);
	        	}
	        	else{
	        		throw new RuntimeException("Tipo de variable desconocida: " + types.get(i).getText());
	        	}
        	}
        
        this.visit(fctx.funcionBloque());
        
        Value returnvalue = this.visit(fctx.funcionBloque().expresion());
    	return returnvalue;
    }

    @Override
    public Value visitAndExpr(LynxParser.AndExprContext ctx) {
        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));
        return new Value(left.asBoolean() && right.asBoolean());
    }

    @Override
    public Value visitOrExpr(LynxParser.OrExprContext ctx) {
        Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));
        return new Value(left.asBoolean() || right.asBoolean());
    }
    
    @Override
    public Value visitPowExpr(LynxParser.PowExprContext ctx) { 
    	Value left = this.visit(ctx.expresion(0));
        Value right = this.visit(ctx.expresion(1));
    	return new Value(Math.pow(left.asDouble(), right.asDouble()));
    }
    
    @Override 
    public Value visitStructExpr(LynxParser.StructExprContext ctx) { 
    	String stvarname = ctx.ID(0).getText();
    	if(!structvars.containsKey(stvarname)) {
            throw new RuntimeException("Esta variable de estructura no existe: " + stvarname);
        }
    	
    	String sttype = structvars.get(stvarname);		
		String stvarattr = ctx.ID(1).getText();
		if(!structmemo.get(sttype).contains(stvarattr)){
			throw new RuntimeException("La estructura " + sttype + " no contiene atributo " + stvarattr);
		}
    	
		String search = "$" + sttype + "@" + stvarname + "@" + stvarattr;
    	Value returnv = memory.get(search) ;
    	return returnv;
    }
    
 // if override
    @Override
    public Value visitSentenciaIf(LynxParser.SentenciaIfContext ctx) {

        List<LynxParser.CondicionesContext> conditions =  ctx.condiciones();
        
        boolean evaluatedBlock = false;

        for(LynxParser.CondicionesContext condition : conditions) {

            Value evaluated = this.visit(condition.expresion());

            if(evaluated.asBoolean()) {
                evaluatedBlock = true;
                // evaluate this block whose expr==true
                this.visit(condition.sentenciasBloque());
                break;
            }
        }

        if(!evaluatedBlock && ctx.sentenciasBloque() != null) {
            // evaluate the else-stat_block (if present == not null)
            this.visit(ctx.sentenciasBloque());
        }

        return Value.VOID;
    }
    
 // while override
    @Override
    public Value visitSentenciaWhile(LynxParser.SentenciaWhileContext ctx) {

        Value value = this.visit(ctx.expresion());

        while(value.asBoolean()) {
        	
            // evaluate the code block
            this.visit(ctx.sentenciasBloque());

            // evaluate the expression
            value = this.visit(ctx.expresion());
        }

        return Value.VOID;
    }
    
   // For override
	@Override 
    public Value visitSentenciaFor(LynxParser.SentenciaForContext ctx) {
   
    	visitAsignador(ctx.asignador());
		String asivar = (ctx.asignador().ID().getText());
    	double start = memory.get(asivar).asDouble();
    	double end = (this.visit(ctx.expresion().getChild(2)).asDouble());
    	String stp = ctx.REAL().getText();
    	double step = Double.parseDouble(stp);
    	
    	if(ctx.expresion().getChild(1).getText().equals("<")){
    		for(double s = start; s < end; s=s+step){
        		// evaluate the code block
                this.visit(ctx.sentenciasBloque());
        		memory.put(asivar, new Value(s+step)); 
            }	
    	}
    	else{
    		for(double s = start; s <= end; s=s+step){
        		// evaluate the code block
                this.visit(ctx.sentenciasBloque());
        		memory.put(asivar, new Value(s+step)); 
            }
    	}
    	
    	return Value.VOID; 
    }
	
	// Function
	@Override 
	public Value visitFuncionX(LynxParser.FuncionXContext ctx) { 
		
		funcmemo.put(ctx.ID().getText(), ctx);
		List<TerminalNode> ids = ctx.parametros().ID();
		
		for(TerminalNode ite: ids){
			memory.put(ite.getText(), null);
		}
	
		return Value.VOID;
	}
	
	//Structures
	@Override 
	public Value visitEstructuras(LynxParser.EstructurasContext ctx) { 
		
		String sname = ctx.ID().getText();
		
		List<LynxParser.DeclaracionContext> svars = ctx.declaracion();
		List<String> attr = new ArrayList<String>(); 
		
		for(LynxParser.DeclaracionContext ite: svars ){
			attr.add(ite.ID().getText());
		}
		
		structmemo.put(sname, attr);		
		
		return Value.VOID;
	}
	
	@Override 
	public Value visitDecstructvar(LynxParser.DecstructvarContext ctx) { 
		
		String stname = ctx.ID(0).getText();
		if(!structmemo.containsKey(stname)) {
            throw new RuntimeException("Esta estructura no existe: " + stname);
        }
		
		String stvarname = ctx.ID(1).getText();
		if(structvars.containsKey(stvarname)) {
            throw new RuntimeException("Esta variable de estructura ya existe: " + stvarname);
        }
		
		structvars.put(stvarname, stname);
		
		return Value.VOID;
	}
	
	@Override 
	public Value visitAsignstruct(LynxParser.AsignstructContext ctx) { 
		
		String stvarname = ctx.ID(0).getText();
		if(!structvars.containsKey(stvarname)) {
            throw new RuntimeException("Esta variable de estructura no existe: " + stvarname);
        }
		String sttype = structvars.get(stvarname);		
		String stvarattr = ctx.ID(1).getText();
		if(!structmemo.get(sttype).contains(stvarattr)){
			throw new RuntimeException("La estructura " + sttype + " no contiene atributo " + stvarattr);
		}
		
		Value attrvalue = this.visit(ctx.expresion());
		String varname = "$" + sttype + "@" + stvarname + "@" + stvarattr;
		memory.put(varname, attrvalue);
		
		return Value.VOID;
	}
	
    
}
