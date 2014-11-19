package Common.Protocol;

/**
 * This class represent a match rule in the MiddleBox, means a pattern (could be regex or string)
 * on which the Middlebox need to act uppon
 * this class serialized to/from json message
 * Created by Lior on 13/11/2014.
 */
public class MatchRule {
    public String pattern; //some exact-string or regular-expression string,
    public boolean is_regex; //true if pattern is a regular-expression or false otherwise,
    public String rid; //rule identification number,

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchRule matchRule = (MatchRule) o;

        if (is_regex != matchRule.is_regex) return false;
        if (!pattern.equals(matchRule.pattern)) return false;
        if (!rid.equals(matchRule.rid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pattern.hashCode();
        result = 31 * result + (is_regex ? 1 : 0);
        result = 31 * result + rid.hashCode();
        return result;
    }
/*
 Future features:
 start-idx: start index in L7 payload,
 end-idx: start index in L7 payload,
 match-sets: array of match sets,
 policy-chains: array of policy chain IDs
 */
}
