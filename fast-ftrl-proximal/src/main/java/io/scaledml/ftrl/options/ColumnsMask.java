package io.scaledml.ftrl.options;

import java.util.ArrayList;

public class ColumnsMask {
    private final String str;
    private ArrayList<ColumnType> columns = new ArrayList<>();

    public ColumnsMask(String str) {
        this.str = str;
        char[] arr = str.toCharArray();
        State state = State.OUT_OF_BRACKET;
        StringBuilder sb = new StringBuilder();
        for (char c : arr) {
            switch (state) {
                case OUT_OF_BRACKET:
                    switch (c) {
                        case 'l':
                        case 'i':
                        case 'n':
                        case 'c':
                            columns.add(charToColumnType(c));
                            break;
                        case '[':
                            state = State.IN_BRACKET;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case IN_BRACKET:
                    switch (c) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case '0':
                            sb.append(c);
                            break;
                        case ']':
                            int repeats = Integer.parseInt(sb.toString()) - 1;
                            ColumnType last = columns.get(columns.size() - 1);
                            for (int i = 0; i < repeats; i++) {
                                columns.add(last);
                            }
                            sb.setLength(0);
                            state = State.OUT_OF_BRACKET;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
            }

        }
    }

    private static ColumnType charToColumnType(char c) {
        switch (c) {
            case 'l':
                return ColumnType.LABEL;
            case 'i':
                return ColumnType.ID;
            case 'n':
                return ColumnType.NUMERICAL;
            case 'c':
                return ColumnType.CATEGORICAL;
            default:
                throw new IllegalArgumentException("" + c);
        }
    }

    @Override
    public String toString() {
        return str;
    }

    public ColumnType getCategory(int colNumber) {
        if (colNumber < columns.size()) {
            return columns.get(colNumber);
        }
        return columns.get(columns.size() - 1);
    }

    public static enum ColumnType {
        LABEL,
        ID,
        NUMERICAL,
        CATEGORICAL
    }

    private static enum State {
        IN_BRACKET, OUT_OF_BRACKET
    }


}
