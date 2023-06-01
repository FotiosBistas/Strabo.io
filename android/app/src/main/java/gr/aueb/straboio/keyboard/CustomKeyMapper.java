package gr.aueb.straboio.keyboard;

import java.util.Arrays;
import java.util.Map;

public class CustomKeyMapper {
    private final Map<Integer, CharGroup<Character, Character, Character, Character>> CODE_TO_LANG = Map.ofEntries(

            // digits
            Map.entry(49, new CharGroup<>('1', '1', '$', '1')),
            Map.entry(50, new CharGroup<>('2', '2', '$', '2')),
            Map.entry(51, new CharGroup<>('3', '3', '$', '3')),
            Map.entry(52, new CharGroup<>('4', '4', '$', '4')),
            Map.entry(53, new CharGroup<>('5', '5', '$', '5')),
            Map.entry(54, new CharGroup<>('6', '6', '$', '6')),
            Map.entry(55, new CharGroup<>('7', '7', '$', '7')),
            Map.entry(56, new CharGroup<>('8', '8', '$', '8')),
            Map.entry(57, new CharGroup<>('9', '9', '$', '9')),
            Map.entry(48, new CharGroup<>('0', '0', '$', '0')),

            // modifier keys
            Map.entry(44, new CharGroup<>(',', ',', '$', ',')),
            Map.entry(32, new CharGroup<>(' ', ' ', '$', ' ')),
            Map.entry(46, new CharGroup<>('.', '.', '$', '.')),

            // first row
            Map.entry(113, new CharGroup<>('q', ';', '$', '~')),
            Map.entry(81, new CharGroup<>('Q', ':', '$', '~')),

            Map.entry(119, new CharGroup<>('w', 'ς', '$', '/')),
            Map.entry(87, new CharGroup<>('W', 'ς', '$', '/')),

            Map.entry(101, new CharGroup<>('e', 'ε', 'έ', '|')),
            Map.entry(69, new CharGroup<>('E', 'Ε', 'Έ', '|')),

            Map.entry(114, new CharGroup<>('r', 'ρ', '$', '\\')),
            Map.entry(82, new CharGroup<>('R', 'Ρ', '$', '\\')),

            Map.entry(116, new CharGroup<>('t', 'τ', '$', '[')),
            Map.entry(84, new CharGroup<>('T', 'Τ', '$', '[')),

            Map.entry(121, new CharGroup<>('y', 'υ', 'ύ', ']')),
            Map.entry(89, new CharGroup<>('Y', 'Υ', 'Ύ', ']')),

            Map.entry(117, new CharGroup<>('u', 'θ', '$', '{')),
            Map.entry(85, new CharGroup<>('U', 'Θ', '$', '{')),

            Map.entry(105, new CharGroup<>('i', 'ι', 'ί', '}')),
            Map.entry(73, new CharGroup<>('I', 'Ι', 'Ί', '}')),

            Map.entry(111, new CharGroup<>('o', 'ο', 'ό', '<')),
            Map.entry(79, new CharGroup<>('O', 'Ο', 'Ό', '<')),

            Map.entry(112, new CharGroup<>('p', 'π', '$', '>')),
            Map.entry(80, new CharGroup<>('P', 'Π', '$', '>')),

            // sencond row
            Map.entry(97, new CharGroup<>('a', 'α', 'ά', '@')),
            Map.entry(65, new CharGroup<>('A', 'Α', 'Ά', '*')),

            Map.entry(115, new CharGroup<>('s', 'σ', '$', '#')),
            Map.entry(83, new CharGroup<>('S', 'Σ', '$', '#')),

            Map.entry(100, new CharGroup<>('d', 'δ', '$', '$')),
            Map.entry(68, new CharGroup<>('D', 'Δ', '$', '€')),

            Map.entry(102, new CharGroup<>('f', 'φ', '$', '_')),
            Map.entry(70, new CharGroup<>('F', 'Φ', '$', '_')),

            Map.entry(103, new CharGroup<>('g', 'γ', '$', '&')),
            Map.entry(71, new CharGroup<>('G', 'Γ', '$', '&')),

            Map.entry(104, new CharGroup<>('h', 'η', 'ή', '-')),
            Map.entry(72, new CharGroup<>('H', 'Η', 'Ή', '-')),

            Map.entry(106, new CharGroup<>('j', 'ξ', '$', '+')),
            Map.entry(74, new CharGroup<>('J', 'Ξ', '$', '+')),

            Map.entry(107, new CharGroup<>('k', 'κ', '$', '(')),
            Map.entry(75, new CharGroup<>('K', 'Κ', '$', '(')),

            Map.entry(108, new CharGroup<>('l', 'λ', '$', ')')),
            Map.entry(76, new CharGroup<>('L', 'Λ', '$', ')')),

            //third row
            Map.entry(122, new CharGroup<>('z', 'ζ', '$', ',')),
            Map.entry(90, new CharGroup<>('Z', 'Ζ', '$', ',')),

            Map.entry(120, new CharGroup<>('x', 'χ', '$', '"')),
            Map.entry(88, new CharGroup<>('X', 'Χ', '$', '"')),

            Map.entry(99, new CharGroup<>('c', 'ψ', '$', '\'')),
            Map.entry(67, new CharGroup<>('C', 'Ψ', '$', '\'')),

            Map.entry(118, new CharGroup<>('v', 'ω', 'ώ', ':')),
            Map.entry(86, new CharGroup<>('V', 'Ω', 'Ώ', ':')),

            Map.entry(98, new CharGroup<>('b', 'β', '$', ';')),
            Map.entry(66, new CharGroup<>('B', 'Β', '$', ';')),

            Map.entry(110, new CharGroup<>('n', 'ν', '$', '!')),
            Map.entry(78, new CharGroup<>('N', 'Ν', '$', '!')),

            Map.entry(109, new CharGroup<>('m', 'μ', '$', '?')),
            Map.entry(77, new CharGroup<>('M', 'Μ', '$', '?'))

    );

    private final int[] KEYBOARD_CODES = new int[] {
            // third row
            122, 90, 120, 88, 99, 67, 118, 86, 98, 66, 110, 78, 109, 77,
            // second row
            97, 65, 115, 83, 100, 68, 102, 70, 103, 71, 104, 72, 106, 74, 107, 75, 108, 76,
            // first row
            113, 81, 119, 87, 101, 69, 114, 82, 116, 84, 121, 89, 117, 85, 105, 73, 111, 79, 112, 80
    };

    public boolean isSwitchable(int code){
        for(int i : KEYBOARD_CODES)
            if(i==code)
                return true;
        return false;
    }

    public char toEN(int code){
        CharGroup tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getFirst() : null;

    }

    public char toGR(int code){
        CharGroup tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getSecond() : null;
    }

    public char toSPECIAL(int code){
        CharGroup tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getThird() : null;
    }

    public char toSYMBOL(int code){
        CharGroup tri = CODE_TO_LANG.get(code);
        return tri != null ? (char) tri.getFourth() : null;
    }

}
