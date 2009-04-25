import java.util.ArrayList;
import java.util.Stack;

public class Equation {
	
	private static final String[] possibles = {"x","pi","e"};
	private static final double tooSmall = .00001;
	private String equation;
	
	/**
	 * Constructor.
	 * @param eqn
	 * The equation you want to evaluate.
	 */
	public Equation(String eqn){
		equation = finish(eqn);
	}
	
	/**
	 * Will change the equation to a new equation.
	 * @param eqn
	 * The new equation.
	 */
	public void changeEqn(String eqn){
		equation = finish(eqn);
	}
	
	/**
	 * This function only calls getAns. The reason I didn't just let the user call this function is because, in order to make the recursion work
	 * without messing up the function, I needed to pass a copy in. I didn't want to make the user do that.
	 * @param x
	 * The numerical value of x that the user wants me to compute. A real number.
	 * @return
	 * The function's output
	 */
	public double findOutput(double x){
		return getAns(equation, x);
	}
	
	/**
	 * This is a method to get the answer out of the function.
	 * @param teq
	 * the equation to evaluate
	 * @param x
	 * the real value of x.
	 * @return
	 * The output of the function.
	 */
	private double getAns(String teq, double x){
		//here is where we will find the inner functions
		//break it into sets. cos[x^(4/3)] + (3*4). cos[x^(4/3)] is a set. so is (3*4).
		int pos;
		String newFunc = "";
		//evaluate parenthesis as their own functions.
		if(teq.indexOf("(") != -1){
			pos = teq.indexOf("(");
			//find it's pair
			int pair = closeLoc(teq,pos);
			String sub = teq.substring(pos,pair+1);
			newFunc = teq.replace(sub, "" + getAns(teq.substring(pos+1,pair),x));
		}
		//evaluate the inside of a function as it's own function, then plug in the number.
		else if(teq.indexOf("[") != -1){
			pos = teq.indexOf("[");
			int pair = closeLoc(teq,pos);
			double eval = getAns(teq.substring(pos+1, pair),x);
			String sub;
			sub = teq.substring(pos-3, pair+1);
			eval = evalFunc(teq.substring(pos-3, pos),eval);
			newFunc = teq.replace(sub, "" + eval);
		}
		//if it doesn't have bracks/parens, then it can be evaluated to a number.
		else
			return makeNum(teq,x);
		
		//maybe...
		newFunc = fixNegs(newFunc);
		
		//call again, because it is not ready yet.
		return getAns(newFunc,x);
	}
	
	/**
	 * 
	 * @param eqn
	 * @param x
	 * @return
	 */
	private double makeNum(String eqn, double x){
		ArrayList<String> equation = new ArrayList<String>();
		
		int nxt = 0;
		while((nxt = nextOp(eqn)) != -1){
			//add the num and op into the arraylist 
			equation.add(eqn.substring(0,nxt));
			equation.add("" + eqn.charAt(nxt));
			//shrink the string
			eqn = eqn.substring(nxt+1);
		}
		equation.add(eqn);
		//now that the string has been converted into an array list
		Stack<Integer> powhotspots = new Stack<Integer>();
		Stack<Integer> mulhotspots = new Stack<Integer>();
		Stack<Integer> sumhotspots = new Stack<Integer>();
		//do a once through, looking for powers
		
		//find all the ops in the string
		//pow
		for(int i = 0; i < equation.size(); i++)
			if(equation.get(i) == "^")
				powhotspots.push(i);
		//fix the zeros
		//equation = fixNegs(equation);
		//do the math for each operator
		equation = evalOps(equation,new char[] {'^'}, x);
		equation = evalOps(equation, new char[] {'/'}, x);
		equation = evalOps(equation, new char[] {'*'}, x);
		equation = evalOps(equation, new char[] {'-'}, x);
		equation = evalOps(equation, new char[] {'+'}, x);
		
		try{
		return Double.parseDouble(equation.get(0));
		}catch(NumberFormatException ex){
			return x;
		}
	}
	
	private String fixNegs(String eqn){
		if(eqn.charAt(0) == '-')
			return "0" + eqn;
		return eqn;
	}
	/**
	 * Finds the next operator in the string
	 * An operator is defined as: +,-,*,/,^
	 * @param eqn
	 * the string to search
	 * @return
	 * the location in the string of the operator
	 */
	private int nextOp(String eqn){
		char [] ops = {'+','-','*','/','^'};
		for(int i = 0; i < eqn.length(); i++){
			char v = eqn.charAt(i);
			for(int o = 0; o < ops.length; o++)
				if(v == ops[o])
					return i;
		}
		return -1;
	}
	
	/**
	 * Finishes parenthesis and brackets in the functions.
	 * Eliminates Whitespace.
	 * Eliminates inferred multiplication.
	 * @param m
	 * The string passed in by the user
	 * @return
	 * The original string with the parenthesis finished.
	 */
	private String finish(String m){
		//close all the open brackets in the group
		Stack<Character> order = new Stack<Character>();
		for(int i = 0; i < m.length(); i++){
			char g = m.charAt(i);
			if(g == '(')
				order.push(')');
			else if(g == '[')
				order.push(']');
			else if(g == ']' || g == ')')
				order.pop();
		}
		while(!order.isEmpty()){
			m += order.pop();
		}
		
		//get rid of white space
		m = m.replace(" ", "");
		
		//get rid of inferred multiplication (4x -> 4*x) or (4pi -> 4*pi)
		
		//reges strings to find the right ops
		String regexString = "[\\x2B\\x2D\\x2F\\x5E\\x2A\\x5B\\x5D\\x50\\x51]"; //the regex string to split on the operators +-*/^()[] (ascii hex)
		String notRegexString = "[^\\x2B\\x2D\\x2F\\x5E\\x2A\\x5B\\x5D\\x50\\x51]"; //the regex string to split on NOT the operators +-*/^()[] (ascii hex)
		//find the operators
		String[] ops = removeBlanks(m.split(notRegexString));
		
		//the everything that's NOT the operators
		String [] splitup = m.split(regexString);
		
		//make any items such as 4x there into 4*x
		//and make sure the last stays last
		/*for(int i = 0; i < splitup.length; i++){
			String tmp = splitup[i];
			if(tmp.length() > 0)
			{
				String myNum;
				boolean contE,contPi,contX,contNo = false;
				contE = tmp.contains("e");
				contPi = tmp.contains("pi");
				contX = tmp.contains("x");
				myNum = numOnly(tmp);
				contNo = myNum.length() > 0;
					
				
				String together = "";
				//makes sure the last item stays in the last place
				switch(tmp.charAt(tmp.length()-1)){
				case 'i': //pi
					together += "pi";
					contPi = false;
					break;
				case 'e':
					together = "e";
					contE = false;
					break;
				case 'x':
					together = "x";
					contX = false;
					break;
				default:
					together = myNum;
					contNo = false;
					break;
				}
				//put the rest in there, if need be.
				if(contNo) together = myNum + "*" + together;
				if(contX) together = "x*" + together;
				if(contPi) together = "pi*" + together;
				if(contE) together = "e*" + together;
				
				splitup[i] = together;
			}
		}*/
		
		//reconstruct the string
		String reconstructed = "";
		for(int i = 0; i < splitup.length; i++){
			reconstructed += splitup[i];
			//add the op
			try{
				reconstructed += ops[i];
			}catch(IndexOutOfBoundsException ex){;}
		}
		
		//replace the constants with the actual numbers
		reconstructed = reconstructed.replace("e", "" + Math.E);
		reconstructed = reconstructed.replace("pi","" + Math.PI);
		return reconstructed;
	}
	
	/**
	 * This will find the matching end bracket for a string
	 * @param m
	 * The string in which you want to find the end place 
	 * @param start
	 * The location of the beginning bracket
	 * @return
	 * the index of the ending one
	 */
	private int closeLoc(String m, int start){
		char g = m.charAt(start);
		char lookFor;
		if(g == '(') lookFor = ')';
		else lookFor = ']';
		
		int tokensLeft = 1;
		int i = start + 1;
		while(i < m.length() && tokensLeft > 0){
			if(m.charAt(i) == lookFor)
				tokensLeft--;
			else if(m.charAt(i) == g)
				tokensLeft++;
			i++;
		}
		return --i;
	}
	
	/**
	 * Takes the requested numbers and ops out of the arraylist and makes a number, then places back into arraylist.
	 * @param equation
	 * separated equation, i.e. 4*x + 3 = [4,*,x,+,3]
	 * @param ops
	 * the ops you want to evaluate, i.e. {'^'} or {'+','-'}
	 * @return
	 * the evaluated equation thin
	 */
	private ArrayList<String> evalOps(ArrayList<String> equation,char [] ops, double x){
		Stack<Integer> tokenlocs = new Stack<Integer>();
		
		//find the places in the equation that the token[s] reside
		for(int i = 0; i < equation.size(); i++)
			for(char ch : ops)
				if(equation.get(i).equals("" + ch))
					tokenlocs.add(i);
		
		while(!tokenlocs.isEmpty()){
			//parse the numbers, find an answer
			int b = tokenlocs.pop();
			double a,c;
			//if it breaks, it's an x
			try{
				a = Double.parseDouble(equation.get(b - 1));
			}catch(NumberFormatException ex){ a = x;}
			try{
				c = Double.parseDouble(equation.get(b + 1));
			}catch(NumberFormatException ex){ c = x;}
			double answer = calculate(equation.get(b),a,c);
			
			//replace x, ^, y with =x^y, and delete the extra places
			equation.set(b-1, "" + answer);
			equation.remove(b+1);
			equation.remove(b);
		}
					
		return equation;
	}
	
	/**
	 * Calculates the two numbers, based on the op that is passed in
	 * @param operation
	 * the char that denotes the operation to use.
	 * @param a1
	 * the first number. This matters, not all operators are commutative
	 * @param a2
	 * the second number. "" 	""			""			""
	 * @return
	 * the result of the operation
	 */
	private double calculate(String operation, double a1, double a2){
		char op = operation.charAt(0);
		switch(op){
		case '^':
			return Math.pow(a1, a2);
		case '+':
			return a1 + a2;
		case '-':
			return a1 - a2;
		case '/':
			return a1/a2;
		case '*':
			return a1*a2;
		default:
			return 0.0;
		}
	}
	
	/**
	 * Will return a copy of the string array with all of the empty ones removed.
	 * @param input
	 * The array [potentially] with blanks
	 * @return
	 * The array without blanks
	 */
	private String[] removeBlanks(String[] input){
		ArrayList<String> out = new ArrayList<String>();
		
		for(int i = 0; i < input.length; i++)
			if(input[i].length() > 0)
				out.add(input[i]);
		
		String[] outArray = new String[out.size()];
		return out.toArray(outArray);
	}
	
	/**
	 * This is where my functions get evaluated.
	 * @param code
	 * The code for each value e.g. cos[pi/2] code = cos
	 * @param value
	 * The input for each value e.g. cos[pi/2] value = pi/2
	 * @return
	 */
	private double evalFunc(String code, double value){
		double retVal;
		//trig functions
		if(code.equals("cos") || code.equals("tan")){
			value = mod2PI(value);
			if(value == (Math.PI/2) || value == 3*Math.PI/2)
				retVal = 0.0;
		}
		if(code.equals("sin")){
			value = mod2PI(value);
			if(value == Math.PI || Math.abs(value) <= tooSmall)
				return 0.0;
			retVal = Math.sin(value);
		}
		else if(code.equals("cos"))
			retVal = Math.cos(value);
		else if(code.equals("sqt"))
			retVal = Math.sqrt(value);
		else if(code.equals("tan"))
			retVal = Math.tan(value);
		//end trig functions
		else if(code.equals("abs")) //absolute value (no surprises there)
			retVal = Math.abs(value);
		else if(code.equals("log")) //natural log
			retVal = Math.log(value);
		else retVal = 0.0;
		
		if(Math.abs(retVal) <= tooSmall)
			return 0.0;
		else
			return retVal;
	}
	
	/**
	 * Returns a copy of the string with only numbers, and the decimal, included.
	 * @param str
	 * The original String with letters, numbers, and whathaveyou
	 * @return
	 * str w/ only the numbers and period
	 */
	private String numOnly(String str){
		String ret = "";
		for(int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			if((c>='0' && c <= '9') || c == '.')
				ret += c;
		}
		return ret;
	}
	
	/**
	 * "Mods" The number by 2pi. 3pi = pi, pi = pi, 3 = 3, 81pi = pi.
	 * @param num
	 * The number you want to have reduced.
	 * @return
	 * The reduced number.
	 */
	private double mod2PI(double num){
		double twopi = 2*Math.PI;
		while(num > twopi)
			num -= twopi;
		return num;
	}
}
