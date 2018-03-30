package com.ldybob.ac3korea.parser;

import com.ldybob.ac3korea.BoardID;
import com.ldybob.ac3korea.parser.impl.FreeBBSParser;
import com.ldybob.ac3korea.parser.impl.OtherBBSParser;
import com.ldybob.ac3korea.parser.impl.QNABBSParser;
import com.ldybob.ac3korea.parser.impl.ScrabParser;

/**
 * 게시판 ID 에 맞춰 Parser 객채 생성하여 return
 */
public class ParserFactory {
    public static IPARSER getParser(String boardID) {
        if (boardID.equals(BoardID.FREE)) {
            return new FreeBBSParser();
        } else if (boardID.equals(BoardID.QNA)) {
            return new QNABBSParser();
        } else if (boardID.equals(BoardID.NEWS) || boardID.equals(BoardID.COMIC) || boardID.equals(BoardID.SCREENSHOT)) {
            return new OtherBBSParser(boardID);
        }else if (boardID.equals(BoardID.SCRAB)) {
            return new ScrabParser();
        }
        return null;
    }
}
