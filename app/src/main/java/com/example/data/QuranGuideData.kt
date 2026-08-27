package com.example.data

enum class GuideLanguage(val label: String, val isRtl: Boolean) {
    ENGLISH("ENG", false),
    URDU("اردو", true),
    ARABIC("العربية", true)
}

data class NuzulFact(
    val title: String,
    val description: String
)

data class NuzulHistoryGuide(
    val mainTitle: String,
    val subtitle: String,
    val facts: List<NuzulFact>
)

data class TajweedRuleItem(
    val name: String,
    val ruleTitle: String,
    val description: String,
    val examples: String? = null
)

data class TajweedGuide(
    val mainTitle: String,
    val subtitle: String,
    val rules: List<TajweedRuleItem>
)

data class WaqfSymbolItem(
    val symbol: String,
    val name: String,
    val instruction: String,
    val explanation: String,
    val isMandatory: Boolean = false
)

data class WaqfGuide(
    val mainTitle: String,
    val subtitle: String,
    val symbols: List<WaqfSymbolItem>
)

object QuranGuideRepository {

    fun getNuzulHistory(lang: GuideLanguage): NuzulHistoryGuide {
        return when (lang) {
            GuideLanguage.ENGLISH -> NuzulHistoryGuide(
                mainTitle = "Quran Revelation & History",
                subtitle = "Timeline of Nuzul, preservation, and structure of the Holy Quran",
                facts = listOf(
                    NuzulFact(
                        title = "First Revelation (610 CE)",
                        description = "The first revelation descended on Monday, 17th Ramadan in the Cave of Hira. Angel Jibreel (AS) conveyed the first 5 verses of Surah Al-'Alaq (96:1–5) to Prophet Muhammad ﷺ."
                    ),
                    NuzulFact(
                        title = "Revelation Period",
                        description = "The Quran was revealed incrementally over ~22 Years, 5 Months, and 14 Days (610 CE – 632 CE). The final verse revealed was Surah Al-Ma'idah (5:3) or Surah Al-Baqarah (2:281)."
                    ),
                    NuzulFact(
                        title = "Structure & Division",
                        description = "114 Surahs total: 86 Makki Surahs (revealed before Hijrah, focusing on Aqeedah & Tawheed) and 28 Madani Surahs (focusing on Shariah & governance). Divided into 30 Juz' (Para), 7 Manzils, 60 Hizbs, and 6,236 Ayahs."
                    ),
                    NuzulFact(
                        title = "Preservation & Codex",
                        description = "Preserved orally by thousands of Sahabah (Huffadh) and written on parchment during the Prophet's ﷺ life. First compiled into a bound book under Khalifah Abu Bakr Siddiq (RA) and standardized as the Uthmani Codex under Khalifah Uthman ibn Affan (RA)."
                    )
                )
            )
            GuideLanguage.URDU -> NuzulHistoryGuide(
                mainTitle = "نزول اور تدوینِ قرآن",
                subtitle = "قرآنِ کریم کا نزول، ساخت اور حفاظت کی مبارک تاریخ",
                facts = listOf(
                    NuzulFact(
                        title = "پہلی وحی (610ء)",
                        description = "غارِ حراء میں 17 رمضان المبارک کو حضرت جبرائیل علیہ السلام نبی کریم ﷺ کی خدمت میں سورۃ العلق کی ابتدائی 5 آیات (96:1–5) لے کر نازل ہوئے۔"
                    ),
                    NuzulFact(
                        title = "مدتِ نزول",
                        description = "قرآنِ کریم تقریباً 22 سال 5 ماہ اور 14 دن (610ء تا 632ء) کے عرصے میں تدریجاً نازل ہوا۔ آخری نازل ہونے والی آیت سورۃ المائدہ 5:3 / البقرہ 2:281 شمار کی جاتی ہے۔"
                    ),
                    NuzulFact(
                        title = "ساخت و تقسیم",
                        description = "کل 114 سورتیں: 86 مکی (عقائد و توحید پر مبنی) اور 28 مدنی (احکام و شریعت پر مبنی)۔ 30 پارے، 7 منازل، 60 حزب اور 6,236 آیات۔"
                    ),
                    NuzulFact(
                        title = "حفاظت و تدوین",
                        description = "عہدِ نبوی ﷺ میں حفاظِ کرام اور کاتبینِ وحی کے ذریعے محفوظ رہا۔ خلیفۂ اول حضرت ابو بکر صدیق رضی اللہ عنہ کے دور میں یکجا کیا گیا اور خلیفۂ سوم حضرت عثمان غنی رضی اللہ عنہ کے دور میں نسخہ عثمانیہ کے تحت امت کو ایک قرائت پر جمع کیا گیا۔"
                    )
                )
            )
            GuideLanguage.ARABIC -> NuzulHistoryGuide(
                mainTitle = "تاريخ النزول والقرآن الكريم",
                subtitle = "نزول القرآن، حفظه، وتدوينه الشريف",
                facts = listOf(
                    NuzulFact(
                        title = "أول ما نزل (610 م)",
                        description = "في عام 610 م (17 رمضان) بغار حراء، نزل أمين الوحي جبريل عليه السلام بأول 5 آيات من سورة العلق (96:1–5) على النبي محمد ﷺ."
                    ),
                    NuzulFact(
                        title = "مدة النزول الشريف",
                        description = "نزل القرآن منجماً على مدى نحو 22 سنة و 5 أشهر و 14 يوماً (610 م - 632 م). وآخر ما نزل قوله تعالى: (اليوم أكملت لكم دينكم) المائدة: 3 / البقرة: 281."
                    ),
                    NuzulFact(
                        title = "التنظيم والسور",
                        description = "114 سورة (86 مكية تهتم بالعقيدة والتوحيد و 28 مدنية تهتم بالأحكام والشرائع)، 30 جزءاً، 7 منازل، 60 حزباً، و 6236 آية."
                    ),
                    NuzulFact(
                        title = "الحفظ والجمع العثماني",
                        description = "حُفظ في الصدور والرقاع في عهد النبوة، وجُمِع في مصحف واحد في عهد الصديق أبي بكر (رضي الله عنه)، وتوحدت المصاحف على المصحف العثماني في عهد عثمان (رضي الله عنه)."
                    )
                )
            )
        }
    }

    fun getTajweedGuide(lang: GuideLanguage): TajweedGuide {
        return when (lang) {
            GuideLanguage.ENGLISH -> TajweedGuide(
                mainTitle = "Rules of Tajweed & Recitation",
                subtitle = "Essential rules for authentic Quran pronunciation",
                rules = listOf(
                    TajweedRuleItem(
                        name = "اظهار",
                        ruleTitle = "Izhar (Clear Pronunciation)",
                        description = "Pronounce Noon Sakinah or Tanween clearly without nasalization before the 6 throat letters: ء هـ ع ح غ خ.",
                        examples = "منْ آمن , أنعمت"
                    ),
                    TajweedRuleItem(
                        name = "ادغام",
                        ruleTitle = "Idgham (Merging Letters)",
                        description = "Merge Noon Sakinah/Tanween into the following letter if it is from Yarmaloon (يرملون: ي, ر, م, ل, و, ن). With Ghunnah for (ينمو) and without Ghunnah for (ر, ل).",
                        examples = "منْ يّقول , منْ رّبهم"
                    ),
                    TajweedRuleItem(
                        name = "اقلاب",
                        ruleTitle = "Iqlab (Conversion to Meem)",
                        description = "Convert Noon Sakinah or Tanween into a light Meem (م) sound with nasalization when followed by letter Ba (ب).",
                        examples = "منْ بَعْدِ -> ممْ بَعْدِ"
                    ),
                    TajweedRuleItem(
                        name = "اخفاء",
                        ruleTitle = "Ikhfa (Concealment)",
                        description = "Pronounce a light nasal hiding sound when Noon Sakinah or Tanween is followed by any of the remaining 15 letters.",
                        examples = "منْ كَانَ , أَنْصَارًا"
                    ),
                    TajweedRuleItem(
                        name = "قلقلة",
                        ruleTitle = "Qalqalah (Echoing Bounce)",
                        description = "Produce a resonant bouncing echo sound when any of the letters of Qutb Jadd (ق ط ب ج د) carries a Sukoon or stop.",
                        examples = "أَحَدٌ , ٱلْفَلَقِ , ٱلْحَقُّ"
                    ),
                    TajweedRuleItem(
                        name = "غنة",
                        ruleTitle = "Ghunnah (Nasal Hum)",
                        description = "Hold a rhythmic 2-count nasal sound when encountering doubled Noon (نّ) or Meem (مّ).",
                        examples = "إنَّ , ثمَّ"
                    ),
                    TajweedRuleItem(
                        name = "مدّ",
                        ruleTitle = "Madd (Vowels Elongation)",
                        description = "Stretch the vowel duration (2, 4, or 6 counts) when letters (ا, و, ي) are followed by Hamzah (ء) or Sukoon.",
                        examples = "السَّمَاءِ , ٱلضَّآلِّينَ"
                    )
                )
            )
            GuideLanguage.URDU -> TajweedGuide(
                mainTitle = "قواعدِ تجوید و ترتیل",
                subtitle = "قرآنِ مجید کی درست ادائیگی کے بنیادی قوانین",
                rules = listOf(
                    TajweedRuleItem(
                        name = "اظہار",
                        ruleTitle = "اظہار (واضح ادائیگی)",
                        description = "حروفِ حلقی (ء هـ ع ح غ خ) سے پہلے نون ساکن یا تنوین آئے تو بغیر غنہ کے نون کی آواز بالکل ظاہر کر کے پڑھیں۔",
                        examples = "منْ آمن , أنعمت"
                    ),
                    TajweedRuleItem(
                        name = "ادغام",
                        ruleTitle = "ادغام (ملا کر پڑھنا)",
                        description = "حروفِ 'یرملون' (ي ر م ل و ن) سے پہلے نون ساکن آئے تو نون کو اگلے حرف میں ملا دیں۔ (ی، ن، م، و میں غنہ کے ساتھ اور ر، ل میں بغیر غنہ)۔",
                        examples = "منْ يّقول , منْ رّبهم"
                    ),
                    TajweedRuleItem(
                        name = "اقلاب",
                        ruleTitle = "اقلاب (میم سے بدلنا)",
                        description = "نون ساکن یا تنوین کے بعد حرف 'ب' آئے تو نون کی آواز کو چھوٹی میم (م) کی آواز سے بدل کر غنہ کے ساتھ پڑھیں۔",
                        examples = "منْ بَعْدِ -> ممْ بَعْدِ"
                    ),
                    TajweedRuleItem(
                        name = "اخفاء",
                        ruleTitle = "اخفاء (ناک میں چھپانا)",
                        description = "باقی 15 حروف کے سامنے نون ساکن یا تنوین آئے تو نون کی آواز کو ناک میں چھپا کر ہلکا غنہ کریں۔",
                        examples = "منْ كَانَ , أَنْصَارًا"
                    ),
                    TajweedRuleItem(
                        name = "قلقلہ",
                        ruleTitle = "قلقلہ (آواز کا پلٹنا)",
                        description = "حروفِ 'قُطْبُ جَدٍّ' (ق ط ب ج د) ساکن ہوں تو ان پر مخرج سے لوٹتی ہوئی پرگوٹ آواز پیدا کریں۔",
                        examples = "أَحَدٌ , ٱلْفَلَقِ , ٱلْحَقُّ"
                    ),
                    TajweedRuleItem(
                        name = "غنہ",
                        ruleTitle = "غنہ (ناک میں آواز ٹھہرانا)",
                        description = "نونِ مشدد (نّ) اور میمِ مشدد (مّ) پر دو حرکات کے برابر ناک میں آواز ٹھہرا کر پڑھنا واجب ہے۔",
                        examples = "إنَّ , ثمَّ"
                    ),
                    TajweedRuleItem(
                        name = "مدّ",
                        ruleTitle = "مدّ (آواز کو لمبا کرنا)",
                        description = "حروفِ مدہ (ا، و، ی) کے بعد ہمزہ یا سکون آئے تو آواز کو 2، 4 یا 6 حرکات تک کھینچ کر پڑھیں۔",
                        examples = "السَّمَاءِ , ٱلضَّآلِّينَ"
                    )
                )
            )
            GuideLanguage.ARABIC -> TajweedGuide(
                mainTitle = "أحكام التجويد الأساسية",
                subtitle = "القواعد النورانية لتلاوة القرآن تلاوة صحيحة",
                rules = listOf(
                    TajweedRuleItem(
                        name = "الإظهار",
                        ruleTitle = "الإظهار الحلقي",
                        description = "إخراج النون الساكنة أو التنوين جلياً من غير غنة عند أحد حروف الحلق الستة: ء هـ ع ح غ خ.",
                        examples = "منْ آمن , أنعمت"
                    ),
                    TajweedRuleItem(
                        name = "الإدغام",
                        ruleTitle = "الإدغام في يرملون",
                        description = "إدخال النون الساكنة أو التنوين في حروف (يرملون). بغنة في (ينمو) وبغير غنة في (ر، ل).",
                        examples = "منْ يّقول , منْ رّبهم"
                    ),
                    TajweedRuleItem(
                        name = "الإقلاب",
                        ruleTitle = "الإقلاب إلى ميم",
                        description = "قلب النون الساكنة أو التنوين ميماً مخفاة بغنة عند التقائها بحرف الباء (ب).",
                        examples = "منْ بَعْدِ -> ممْ بَعْدِ"
                    ),
                    TajweedRuleItem(
                        name = "الإخفاء",
                        ruleTitle = "الإخفاء الحقيقي",
                        description = "نطق النون الساكنة بصفة بين الإظهار والإدغام مع بقاء الغنة عند بقية الـ 15 حرفاً.",
                        examples = "منْ كَانَ , أَنْصَارًا"
                    ),
                    TajweedRuleItem(
                        name = "القلقلة",
                        ruleTitle = "القلقلة والاضطراب",
                        description = "اضطراب الصوت وتحريكه عند النطق بحروف (قطب جد) وهي ساكنة ليسمع لها نبرة قوية.",
                        examples = "أَحَدٌ , ٱلْفَلَقِ , ٱلْحَقُّ"
                    ),
                    TajweedRuleItem(
                        name = "الغنة",
                        ruleTitle = "الغنة المشددة",
                        description = "صوت رخيم يخرج من الخيشوم بمقدار حركتين عند النون والميم المشددتين (نّ، مّ).",
                        examples = "إنَّ , ثمَّ"
                    ),
                    TajweedRuleItem(
                        name = "المد",
                        ruleTitle = "أحكام المدود",
                        description = "إطالة الصوت بحرف من حروف المد (ا، و، ي) بمقدار 2 أو 4 أو 6 حركات عند وجود همز أو سكون.",
                        examples = "السَّمَاءِ , ٱلضَّآلِّينَ"
                    )
                )
            )
        }
    }

    fun getWaqfGuide(lang: GuideLanguage): WaqfGuide {
        return when (lang) {
            GuideLanguage.ENGLISH -> WaqfGuide(
                mainTitle = "Quran Stop & Pause Symbols (Waqf)",
                subtitle = "Rules for pausing and continuing while reciting the Quran",
                symbols = listOf(
                    WaqfSymbolItem(
                        symbol = "م",
                        name = "Waqf Lazim",
                        instruction = "MUST STOP (Compulsory)",
                        explanation = "A mandatory pause. Continuing your recitation here will alter or distort the intended Quranic meaning.",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "ط",
                        name = "Waqf Mutlaq",
                        instruction = "RECOMMENDED PAUSE",
                        explanation = "An absolute sentence boundary. Stopping here to take a breath is highly recommended.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ج",
                        name = "Waqf Ja'iz",
                        instruction = "PERMISSIBLE PAUSE",
                        explanation = "An optional stop. Both pausing and continuing without stopping are equally permissible.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "قلي",
                        name = "Al-Waqfu Awla",
                        instruction = "PAUSE PREFERRED",
                        explanation = "Stopping here is better and preferable over continuing, though continuing is allowed.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "صلي",
                        name = "Al-Waslu Awla",
                        instruction = "CONTINUE PREFERRED",
                        explanation = "Continuing recitation without stopping is better, though pausing is allowed.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "لا",
                        name = "Waqf Mamnu'",
                        instruction = "DO NOT STOP (Forbidden)",
                        explanation = "Forbidden to stop here. If you run out of breath and stop, repeat the previous word to preserve meaning.",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "∴ ... ∴",
                        name = "Mu'anaqah",
                        instruction = "PAIRED STOP",
                        explanation = "You must stop at ONE of the two symbol pairs in the verse, but NEVER stop at both.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "س",
                        name = "Saktah",
                        instruction = "SILENT BRIEF PAUSE",
                        explanation = "Pause briefly for a second WITHOUT taking a fresh breath, then continue reading.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ع",
                        name = "Ruku' Symbol",
                        instruction = "THEMATIC SECTION END",
                        explanation = "Marks the completion of a passage/subject. An ideal place to end recitation in Salah.",
                        isMandatory = false
                    )
                )
            )
            GuideLanguage.URDU -> WaqfGuide(
                mainTitle = "علاماتِ وقف و ترتیل",
                subtitle = "قرآنِ مجید میں ٹھہرنے اور ملا کر پڑھنے کی علامات",
                symbols = listOf(
                    WaqfSymbolItem(
                        symbol = "م",
                        name = "وقفِ لازم",
                        instruction = "لازمی ٹھہریں (واجب)",
                        explanation = "یہاں ٹھہرنا ضروری ہے۔ اگر ملا کر پڑھیں گے تو قرآنی مفہوم بدلنے یا فاسد ہونے کا اندیشہ ہے۔",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "ط",
                        name = "وقفِ مطلق",
                        instruction = "ٹھہرنا بہتر ہے",
                        explanation = "یہاں بات مکمل ہو چکی ہے۔ ٹھہر کر سانس لینا زیادہ اولیٰ اور افضل ہے۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ج",
                        name = "وقفِ جائز",
                        instruction = "ٹھہرنا اور ملانا برابر ہے",
                        explanation = "یہاں ٹھہرنا اور بغیر ٹھہرے آگے پڑھنا دونوں حالتیں یکساں جائز ہیں۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "قلي",
                        name = "الوقف أولى",
                        instruction = "ٹھہرنا زیادہ بہتر ہے",
                        explanation = "یہاں ٹھہرنا ملا کر پڑھنے کے مقابلے میں زیادہ افضل اور بہتر ہے۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "صلي",
                        name = "الوصل أولى",
                        instruction = "ملا کر پڑھنا زیادہ بہتر ہے",
                        explanation = "یہاں ملا کر پڑھنا ٹھہرنے کے مقابلے میں زیادہ افضل ہے۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "لا",
                        name = "وقفِ ممنوع",
                        instruction = "ہرگز نہ ٹھہریں",
                        explanation = "یہاں ٹھہرنا منع ہے۔ اگر سانس ٹوٹ جائے تو پچھلا لفظ ملا کر دوبارہ پڑھیں۔",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "∴ ... ∴",
                        name = "معانقہ (جوڑا)",
                        instruction = "ایک جگہ ٹھہریں",
                        explanation = "ان دو علامات میں سے کسی ایک جگہ ٹھہریں، دونوں جگہوں پر ہرگز نہ ٹھہریں۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "س",
                        name = "سکتہ",
                        instruction = "بغیر سانس توڑے سکتہ",
                        explanation = "سانس توڑے۔ بغیر تھوڑی دیر کے لیے آواز روک کر آگے پڑھیں۔",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ع",
                        name = "علامتِ رکوع",
                        instruction = "مکمل مضمون / رکوع",
                        explanation = "کسی موضوع یا رکوع کے خاتمے کی علامت۔ نماز میں رکوع کرنے کے لیے بہترین جگہ۔",
                        isMandatory = false
                    )
                )
            )
            GuideLanguage.ARABIC -> WaqfGuide(
                mainTitle = "علامات الوقف في المصحف الشريف",
                subtitle = "دليل الوقوف والوصل أثناء تلاوة القرآن الكريم",
                symbols = listOf(
                    WaqfSymbolItem(
                        symbol = "م",
                        name = "الوقف اللازم",
                        instruction = "يلزم الوقف (واجب)",
                        explanation = "وقف واجب لئلا يتغير المعنى المراد من الآية الكريمة.",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "ط",
                        name = "الوقف المطلق",
                        instruction = "الوقف أولى وأفضل",
                        explanation = "الكلام تام ومستقل، يفضل الوقف عنده وأخذ النفس.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ج",
                        name = "الوقف الجائز",
                        instruction = "جواز الوقف والوصل",
                        explanation = "استواء الطرفين، يجوز الوقف ويجوز الوصل بدون ترجيح.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "قلي",
                        name = "الوقف أولى",
                        instruction = "الوقف راجح",
                        explanation = "الوقف عنده أولى وأفضل من الوصل.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "صلي",
                        name = "الوصل أولى",
                        instruction = "الوصل راجح",
                        explanation = "الوصل عنده أولى وأفضل من الوقف.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "لا",
                        name = "الوقف الممنوع",
                        instruction = "لا تقف إلا لضرورة",
                        explanation = "يمنع الوقف هنا لتعلق المعنى بما بعده. وإن وقفت لضرورة فأعد الكلمة.",
                        isMandatory = true
                    ),
                    WaqfSymbolItem(
                        symbol = "∴ ... ∴",
                        name = "وقف المعانقة",
                        instruction = "قف على أحدهما",
                        explanation = "إذا وقفت على الموضع الأول لا تقف على الثاني، والعكس صحيح.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "س",
                        name = "السكتة اللطيفة",
                        instruction = "سكت بدون تنفس",
                        explanation = "قطع الصوت زمناً يسيراً دون أخذ نفس ثم مواصلة القراءة.",
                        isMandatory = false
                    ),
                    WaqfSymbolItem(
                        symbol = "ع",
                        name = "علامة الركوع",
                        instruction = "خاتمة المقصد والركوع",
                        explanation = "تدل على انتهاء المعنى أو المقطع في المصحف، وموضع مناسب للركوع في الصلاة.",
                        isMandatory = false
                    )
                )
            )
        }
    }
}
