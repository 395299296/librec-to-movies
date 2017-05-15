// JavaScript Document

function ZoomPic ()
{
    this.initialize.apply(this, arguments)  
}
ZoomPic.prototype = 
{
    initialize : function (id)
    {
        var _this = this;
        this.wrap = typeof id === "string" ? document.getElementById(id) : id;
        this.oUl = this.wrap.getElementsByTagName("ul")[0];
        this.aLi = this.wrap.getElementsByTagName("li");
        this.pres = this.wrap.getElementsByTagName("pre")
        this.prev = this.pres[0];
        this.next = this.pres[1];
        this.timer = null;
        this.aSort = [];
        this.iCenter = 3;
        this._doPrev = function () {return _this.doPrev.apply(_this)};
        this._doNext = function () {return _this.doNext.apply(_this)};
        for (var i = 0; i < this.aLi.length; i++) this.aSort[i] = this.aLi[i];
        this.aSort.unshift(this.aSort.pop());
        var width = parseFloat(this.css(this.wrap, "width"));
        var r = 1.2;
        var x1 = width / (r*r + 2*r + 6.8)
        var left = 0;
        var height = x1 * r * r * 1.5 + 60;
        this.css(this.wrap, "height", height);
        this.options = [];
        for (var i = 0; i < this.aSort.length; i++){
            if (i >= 7) break;
            var w = x1;
            if (i == 2 || i == 4) {
                w = x1 * r;
            }
            if (i == 3) {
                w = x1 * r * r;
            }
            var index = i <=3 ? i + 1 : 7 - i;
            left += x1 / 6;
            var top = (x1 * r * r - w) * 1.5 / 2 + 30;
            this.css(this.aSort[i], "display", "block");
            this.css(this.aSort[i], "width", w);
            this.css(this.aSort[i], "height", w*1.5);
            this.css(this.aSort[i], "left", left);
            this.css(this.aSort[i], "top", top);
            this.options[i] = {width:w, height:w*1.5, top:top, left:left, zIndex:index},
            left += w + x1 / 6;
        }
        for (var i = 0; i < this.pres.length; i++){
            this.css(this.pres[i], "width", x1/5);
            this.css(this.pres[i], "height", x1*2/3);
            this.css(this.pres[i], "margin-top", -x1/3-10);
            this.css(this.pres[i], "line-height", x1*2/3);
        }
        this.setUp();
        this.addEvent(this.prev, "click", this._doPrev);
        this.addEvent(this.next, "click", this._doNext);
        this.doImgClick();      
        this.timer = setInterval(function ()
        {
            _this.doNext()  
        }, 3000);       
        this.wrap.onmouseover = function ()
        {
            clearInterval(_this.timer)  
        };
        this.wrap.onmouseout = function ()
        {
            _this.timer = setInterval(function ()
            {
                _this.doNext()  
            }, 3000);   
        }
    },
    doPrev : function ()
    {
        this.aSort.unshift(this.aSort.pop());
        this.setUp()
    },
    doNext : function ()
    {
        this.aSort.push(this.aSort.shift());
        this.setUp()
    },
    doImgClick : function ()
    {
        var _this = this;
        for (var i = 0; i < this.aSort.length; i++)
        {
            this.aSort[i].onclick = function ()
            {
                if (this.index > _this.iCenter)
                {
                    for (var i = 0; i < this.index - _this.iCenter; i++) _this.aSort.push(_this.aSort.shift());
                    _this.setUp()
                }
                else if(this.index < _this.iCenter)
                {
                    for (var i = 0; i < _this.iCenter - this.index; i++) _this.aSort.unshift(_this.aSort.pop());
                    _this.setUp()
                }
            }
        }
    },
    setUp : function ()
    {
        var _this = this;
        var i = 0;
        for (i = 0; i < this.aSort.length; i++) this.oUl.appendChild(this.aSort[i]);
        for (i = 0; i < this.aSort.length; i++)
        {
            this.aSort[i].index = i;
            if (i < 7)
            {
                this.css(this.aSort[i], "display", "block");
                this.doMove(this.aSort[i], this.options[i], function ()
                {
                    _this.aSort[_this.iCenter].onmouseover = function ()
                    {
                        _this.doMove(this.getElementsByTagName("div")[0], {bottom:0})
                    };
                    _this.aSort[_this.iCenter].onmouseout = function ()
                    {
                        _this.doMove(this.getElementsByTagName("div")[0], {bottom:-100})
                    }
                });
            }
            else
            {
                this.css(this.aSort[i], "display", "none");
                this.css(this.aSort[i], "width", 0);
                this.css(this.aSort[i], "height", 0);
                this.css(this.aSort[i], "top", 37);
                this.css(this.aSort[i], "left", this.oUl.offsetWidth / 2)
            }
        }       
    },
    addEvent : function (oElement, sEventType, fnHandler)
    {
        return oElement.addEventListener ? oElement.addEventListener(sEventType, fnHandler, false) : oElement.attachEvent("on" + sEventType, fnHandler)
    },
    css : function (oElement, attr, value)
    {
        if (arguments.length == 2)
        {
            return oElement.currentStyle ? oElement.currentStyle[attr] : getComputedStyle(oElement, null)[attr]
        }
        else if (arguments.length == 3)
        {
            switch (attr)
            {
                case "width":
                case "height":
                case "top":
                case "left":
                case "bottom":
                case "margin-top":
                case "line-height":
                    oElement.style[attr] = value + "px";
                    break;
                default :
                    oElement.style[attr] = value;
                    break
            }   
        }
    },
    doMove : function (oElement, oAttr, fnCallBack)
    {
        var _this = this;
        clearInterval(oElement.timer);
        oElement.timer = setInterval(function ()
        {
            var bStop = true;
            for (var property in oAttr)
            {
                var iCur = parseFloat(_this.css(oElement, property));
                var iSpeed = (oAttr[property] - iCur) / 5;
                iSpeed = iSpeed > 0 ? Math.ceil(iSpeed) : Math.floor(iSpeed);
                
                if (iCur != oAttr[property])
                {
                    bStop = false;
                    if (Math.abs(iCur - oAttr[property]) < 1)
                        _this.css(oElement, property, oAttr[property]);
                    else
                        _this.css(oElement, property, iCur + iSpeed);
                }
            }
            if (bStop)
            {
                clearInterval(oElement.timer);
                fnCallBack && fnCallBack.apply(_this, arguments)    
            }
        }, 30)
    }
};
window.onload = function ()
{
    new ZoomPic("box");
};