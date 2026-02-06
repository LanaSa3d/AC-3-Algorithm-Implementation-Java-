
package ai_bonuscode;

import java.util.*;

public class AC3_Algorithm_LanaSaad {
    

// Constraint object: represents a rule "leftVar operator rightVar"
    static class Constraint {

        String leftVar;      // X in "X op Y"
        String rightVar;     // Y in "X op Y"
        String operator;     // = , != , < , > , <= , >=
                                                                          
        Constraint(String left, String right, String op) {
            this.leftVar = left;
            this.rightVar = right;
            this.operator = op;
        }
    }

    // Reverse operator for creating reverse constraints (A < B becomes B > A)
    static String reverseOperator(String op) {
        switch (op) {
            case "<":
                return ">";
            case ">":
                return "<";
            case "<=":
                return ">=";
            case ">=":
                return "<=";
            case "=":
                return "=";
            case "!=":
                return "!=";
            default:
                return op;
        }
    }

    // Check if a pair of values satisfies a relation
    static boolean satisfies(int xValue, int yValue, String operator) { // this method checks if the given xValue and yValue satisfy the constraint operator.
        switch (operator) {
            case "=":
                return xValue == yValue;
            case "!=":
                return xValue != yValue;
            case "<":
                return xValue < yValue;
            case ">":
                return xValue > yValue;
            case "<=":
                return xValue <= yValue;
            case ">=":
                return xValue >= yValue;
            default:
                return false;
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Read number of variables
        System.out.print("Enter number of variables: ");
        int variableCount = input.nextInt();
        input.nextLine();

        // Read variable names
        List<String> variableNames = new LinkedList<>(); // we made a linked list to store the variable names.
        for (int i = 0; i < variableCount; i++) {
            System.out.print("Enter variable " + (i + 1) + ": ");
            variableNames.add(input.nextLine().trim()); // we used trim() to avoid mistakes with spaces before or after the variable name.
        }

        // Read domains
        Map<String, List<Integer>> domains = new HashMap<>(); // we are using HashMap to store the domains of each variable. like A: [1,2,3]. HashMap<> will create an empty map to store the domains -> domains = { }. An example domains.put("A", [1,2,3]) or domains.put("B", [2,3,4])
        for (int i = 0; i < variableNames.size(); i++) {

            String variable = variableNames.get(i);
            System.out.print("Enter domain for " + variable + " (space-separated): ");

            String[] tokens = input.nextLine().trim().split(" "); // this will make an array of strings by splitting the input string by spaces and it will be a domain for variable x.

            // Convert to integers safely
            List<Integer> domainValues = new LinkedList<>(); // we are using linked list to store the domain values of each variable. like [1,2,3]
            for (int x = 0; x < tokens.length; x++) {
                if (!tokens[x].isEmpty()) { // while the tokens array is not empty, we will add it to the domain values list.
                    domainValues.add(Integer.parseInt(tokens[x]));
                }
            }

            domains.put(variable, domainValues); // we used put() instead of add() because we are using HashMap to store the domains of each variable.
        }

        // Show the allowed operators to the user
        System.out.println("\nAllowed operators: =  !=  <  >  <=  >=");

        // Read constraints
        System.out.print("\nEnter number of constraints: ");
        int constraintCount = input.nextInt();
        input.nextLine();

        List<Constraint> constraints = new LinkedList<>(); // this will store all the constraints. we used <Constraint> to specify that this linked list will store Constraint type objects. and we can later user new Constraint(left, right, op) to add constraints to this list.

        for (int i = 0; i < constraintCount; i++) {

            System.out.print("Enter constraint " + (i + 1) + " (X operator Y): ");

            // Example: A < B
            String[] parts = input.nextLine().trim().split(" "); // this will split the input string by spaces and store it in parts array like A > B -> parts[0] = A, parts[1] = >, parts[2] = B

            String left = parts[0];
            String op = parts[1];
            String right = parts[2];

            // Add forward constraint
            constraints.add(new Constraint(left, right, op)); // this will add a new constraint to the constraints list like Left = A, Right = B, operator = >

            // Add reverse constraint
            constraints.add(new Constraint(right, left, reverseOperator(op)));
        }

        // Initialize AC-3 queue with all constraints
        Queue<Constraint> queue = new LinkedList<>(constraints); // we used <constraint> to specify that this queue will store Constraint type objects that we created earlier. and (constraints) will add all the constraints we made to the queue.

        // AC-3 main loop
        while (!queue.isEmpty()) {

            Constraint current = queue.poll(); // get and remove the head of the queue so the first constraint in the queue is stored in current variable.

            // If revising changes the domain, re-add related constraints
            if (revise(domains, current)) {  // this will call the revise method with curent constraint and domains map as parameters.

                for (int i = 0; i < constraints.size(); i++) {
                    Constraint other = constraints.get(i); // here we are getting each constraint from the constraints list one by one.

                    // Add if the right side depends on updated leftVar
                    if (other.rightVar.equals(current.leftVar)) { // then here we are checking if the right variable of the "other" constraint is equal to the left variable of the "current" constraint.
                        queue.add(other);
                    }
                }
            }
        }

        // Output final domains
        System.out.println("\nFinal Domains After AC-3:");
        for (int i = 0; i < variableNames.size(); i++) {
            String variable = variableNames.get(i);
            System.out.println(variable + " : " + domains.get(variable));
        }
    }

    // Revise: enforce arc consistency for one constraint
    static boolean revise(Map<String, List<Integer>> domains, Constraint c) { // we are using static because we are calling this method from the main method which is also static. instead of creating an object to call it we can just call it directly.

        boolean domainChanged = false; // flag to track if any value was removed or the domain changed.

        // Copy X domain (safe looping)
        List<Integer> leftDomainCopy = new LinkedList<>(domains.get(c.leftVar)); // we are creating a copy of the left variable's domain to avoid concurrent modification issues while iterating and modifying the original domain.

        // Direct reference to Y domain
        List<Integer> rightDomain = domains.get(c.rightVar); // we are creating a direct reference to the right variable's domain since we don't need to modify it we are just checking values against it.

        // For each value in X
        for (int i = 0; i < leftDomainCopy.size(); i++) {

            int xValue = leftDomainCopy.get(i); // we are getting each value of the left variable's domain one by one.
            boolean supported = false; // supported is used to check if there is any value in Y that satisfies the constraint with the current xValue. at least one value.

            // Check if ANY value in Y satisfies X op Y
            for (int j = 0; j < rightDomain.size(); j++) {
                int yValue = rightDomain.get(j); // we are getting each value of the right variable's domain one by one.

                if (satisfies(xValue, yValue, c.operator)) { // we are calling the satisfies method to check if the current xValue and yValue satisfy the constraint operator.
                    supported = true; // if we find at least one value in Y that satisfies the constraint with xValue, we set supported to true.
                    break; // no need to check further, we found a support yValue. so we go the next xValue. and we break the inner loop.
                }
            }

            // If no support value found, remove xValue
            if (!supported) { // if false, it means there is no value in Y that satisfies the constraint with xValue. so we remove xValue from the left variable's domain.
                domains.get(c.leftVar).remove(Integer.valueOf(xValue)); // we are removing xValue from the original domain of the left variable.
                domainChanged = true; // we set domainChanged to true because we removed a value from the domain.
            }
        }

        return domainChanged;
    }
}