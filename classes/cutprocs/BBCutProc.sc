//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutProc N.M.Collins 17/10/01

//24/11/01 added newPhraseAccounting, endBlockAccounting functions to avoid code reuse
//phraselength taken on by base class- no problems since all these responsibilities can still be taken
//on explicitly by derived classes

//making cuts [delta,duration] (notes 10/11/01) would only take minor corrections,
//in the procs and in BBCut main class. No procedure uses this currently and
//unlikely since any constant/proportional duty cycle is already possible with standard enveloping
//in future may want systematic control- ultimately may want [delta,dur,amp,offset] procedural control

//base class is also the NoCutProc (or the OneCutProc= whole phrase)

BBCutProc : Stream {
    var <phrase,<block,<totalbeatsdone;
    var <cuts,<blocklength,<phrasepos,<beatspersubdiv;
    var <phraseprop, <offset, <roll;
    var <currphraselength,<phraselength;

    *new { |bpsd = 0.5, phraselength = 4.0|
        ^super.new.initBBCutProc(bpsd, phraselength)
    }

    initBBCutProc { |bpsd = 0.5, pl = 4.0| //default to quavers

        phrase = 0;
        block = 0;
        beatspersubdiv = bpsd;
        phraselength = pl;
        phrasepos = 0.0;
        //must start 0.0 so to trigger a new phrase immediately
        currphraselength = 0.0;
        totalbeatsdone = 0.0;
    }

    chooseblock {
        this.newPhraseAccounting;

        //each phrase has one block
        blocklength = currphraselength;
        cuts = [blocklength];
   
        this.endBlockAccounting;
    }

    next {
        var b;

        // The current setup is to set properties of the CutProc and them copy them over to the block.
        this.chooseblock;

        // put into a BBCutBlock for rendering purposes
        b = BBCutBlock();
        b.blocknum = block;
        b.length = blocklength;
        // 'phrasepos' property of BBCutProc is position of the END of the block
        // while 'phrasepos' property of BBCutBlock is position of the START of the block.
        // Will rename eventually.
        b.phrasepos = phrasepos - blocklength;
        b.offset = offset;
        b.isroll = roll;
        b.currphraselength = currphraselength;
        b.phraseprop = phrasepos / currphraselength;
        // Cuts take on the form [interval, duration, offsetparam, amplitude]
        // A simple number becomes [x, x, nil, 1]
        b.cuts = cuts.collect { |cut|
            cut.isKindOf(Number).if { [cut, cut, nil, 1] } { cut };
        };

        // Redundant for now, but might as well keep it
        b.update;

        ^b;
    }

    // backward compatibility
    getBlock {
        ^this.next;
    }
    
    phraseover {
        ^if(((currphraselength - phrasepos) < 0.00001), 1, 0)
    }

    //avoids repeated common code

    //done manually in MultiProc to avoid updatephrase call
    //currphraselength recalculated in BBCutProc11 and may be for future procedures
    newPhraseAccounting { |argCurrentPhraseLength|
        //if nil nothing was passed in; o/w, for procedures that work out the new phrase length
        //themselves can pass in chosen phraselength

        currphraselength = argCurrentPhraseLength ? phraselength.value(phrase);

        phrasepos = 0.0;
        phrase = phrase + 1;
        block = 0;
    }

    endBlockAccounting {
        phrasepos = phrasepos + blocklength;
        totalbeatsdone = totalbeatsdone + blocklength;
        block = block + 1;
    }

}