package br.imd.ufrn.Tests;

import br.imd.ufrn.Annotations.Body;
import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.Param;
import br.imd.ufrn.Annotations.RemoteService;

@RemoteService("/calc")
public class Service {
    @Get("/add")
    public Sum getMethod(@Param("num1") int num1, @Param("num2") int num2, @Body TestObject testObject) {
        Sum sum = new Sum(num1, num2, testObject.getTest());
        System.out.println("Executando método getMethod com anotacao add!");
        return sum;
    }
}
