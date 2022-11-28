
#define va_arg(list, mode) ((mode *)(list = (char *)list + sizeof(mode)))[-1]
#define va_copy(d,s) ((d) = (s))
#define _INTSIZEOF(n)   ( (sizeof(n) + sizeof(int) - 1) & ~(sizeof(int) - 1) )
#define va_start(ap,v)  ( ap = (va_list)&v + _INTSIZEOF(v) )
