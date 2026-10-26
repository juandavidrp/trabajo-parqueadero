package app;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Main {

    static final int CAPACIDAD_TOTAL = 10;
    static final int TARIFA_HORA_CARRO = 4000;
    static final int TARIFA_DIA_CARRO = 30000;
    static final int TARIFA_HORA_MOTO = 2000;
    static final int TARIFA_DIA_MOTO = 15000;
    static final int UMBRAL_HORAS_DIA = 8;
    static final int HORA_APERTURA = 8;
    static final int HORA_CIERRE = 22;

    static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static final Scanner SCANNER = new Scanner(System.in);

    static String[] tiposVehiculos = new String[CAPACIDAD_TOTAL];
    static LocalDateTime[] fechasIngreso = new LocalDateTime[CAPACIDAD_TOTAL];

    static int recaudoCarros = 0;
    static int recaudoMotos = 0;

    static int buscarCeldaDisponible() {
        for (int i = 0; i < CAPACIDAD_TOTAL; i++) {
            if (tiposVehiculos[i] == null) {
                return i;
            }
        }
        return -1;
    }

    static	LocalDateTime	leerFechaHora(String	mensaje)	{
        while	(true)	{
            System.out.print(mensaje);
            String	texto	=	SCANNER.nextLine().trim();
            if	(!formatoFechaHoraValido(texto))	{
                imprimir("Fecha	u	hora	inválidas.	Use	yyyy-MM-dd	HH:mm	y	el	horario	08:00-22:00.");
                continue;
            }
            return	LocalDateTime.parse(texto,	FORMATO_FECHA_HORA);
        }
    }

    static	boolean	formatoFechaHoraValido(String	texto)	{
        if	(texto.length()	!=	16
                ||	texto.charAt(4)	!=	'-'
                ||	texto.charAt(7)	!=	'-'
                ||	texto.charAt(10)	!=	'	'
                ||	texto.charAt(13)	!=	':')	{
            return	false;
        }
        int	anio	=	leerDigitos(texto,	0,	4);
        int	mes	=	leerDigitos(texto,	5,	2);
        int	dia	=	leerDigitos(texto,	8,	2);
        int	hora	=	leerDigitos(texto,	11,	2);
        int	minuto	=	leerDigitos(texto,	14,	2);
        return	anio	>=	1	&&	mes	>=	1	&&	mes	<=	12	&&	dia	>=	1	&&	dia	<=	31
                &&	hora	>=	HORA_APERTURA	&&	hora	<=	HORA_CIERRE
                &&	(hora	!=	HORA_CIERRE	||	minuto	<=	0)
                &&	minuto	>=	0	&&	minuto	<=	59;
    }

    static	int	leerDigitos(String	texto,	int	inicio,	int	cantidad)	{
        int	valor	=	0;
        for	(int	i	=	inicio;	i	<	inicio	+	cantidad;	i++)	{
            char	caracter	=	texto.charAt(i);
            if	(caracter	<	'0'	||	caracter	>	'9')	{
                return	-1;
            }
            valor	=	valor	*	10	+	caracter	-	'0';
        }
        return	valor;
    }

    static void registrarEntrada() {
        int celda = buscarCeldaDisponible();
        if (celda == -1) {
            System.out.println("El parqueadero está lleno. No hay celdas disponibles.");
            return;
        }

        System.out.print("Ingrese el tipo de vehículo (CARRO / MOTO): ");
        String tipo = SCANNER.nextLine().trim().toUpperCase();

        if (!tipo.equals("CARRO") && !tipo.equals("MOTO")) {
            System.out.println("Tipo de vehículo inválido.");
            return;
        }

        LocalDateTime ingreso = leerFechaHora("Ingrese la fecha y hora de ingreso (yyyy-MM-dd HH:mm): ");


        tiposVehiculos[celda] = tipo;
        fechasIngreso[celda] = ingreso;

        System.out.println("¡Vehículo registrado con éxito en la celda #" + (celda + 1) + "!");
    }

    static void registrarSalida() {
        System.out.print("Ingrese el número de celda a liberar (1 a " + CAPACIDAD_TOTAL + "): ");
        int numeroCelda = Integer.parseInt(SCANNER.nextLine());
        int indice = numeroCelda - 1;

        if (indice < 0 || indice >= CAPACIDAD_TOTAL || tiposVehiculos[indice] == null) {
            System.out.println("Celda inválida o se encuentra libre actualmente.");
            return;
        }

        LocalDateTime ingreso = fechasIngreso[indice];
        LocalDateTime salida = leerFechaHora("Ingrese la fecha y hora de salida (yyyy-MM-dd HH:mm): ");

        if (salida.isBefore(ingreso)) {
            System.out.println("Error: La fecha de salida no puede ser anterior al ingreso.");
            return;
        }


        long diasACobrar = ChronoUnit.DAYS.between(ingreso, salida);
        LocalDateTime cursor = ingreso.plusDays(diasACobrar);
        long minutosRestantes = Duration.between(cursor, salida).toMinutes();
        long horasACobrar = (minutosRestantes + 59) / 60;

        if (horasACobrar >= UMBRAL_HORAS_DIA) {
            diasACobrar++;
            horasACobrar = 0;
        }


        String tipo = tiposVehiculos[indice];
        int tarifaDia = tipo.equals("CARRO") ? TARIFA_DIA_CARRO : TARIFA_DIA_MOTO;
        int tarifaHora = tipo.equals("CARRO") ? TARIFA_HORA_CARRO : TARIFA_HORA_MOTO;

        int totalDiasCobro = (int) diasACobrar * tarifaDia;
        int totalHorasCobro = (int) horasACobrar * tarifaHora;
        int totalPagar = totalDiasCobro + totalHorasCobro;


        System.out.println("\n========== FACTURA DE SALIDA ==========");
        System.out.println("Celda: " + numeroCelda);
        System.out.println("Tipo de vehículo: " + tipo);
        System.out.println("Ingreso: " + ingreso.format(FORMATO_FECHA_HORA));
        System.out.println("Salida:  " + salida.format(FORMATO_FECHA_HORA));
        System.out.println("Días cobrados (" + diasACobrar + "): $" + totalDiasCobro);
        System.out.println("Horas cobradas (" + horasACobrar + "): $" + totalHorasCobro);
        System.out.println("TOTAL A PAGAR:       $" + totalPagar);
        System.out.println("=======================================");


        if (tipo.equals("CARRO")) {
            recaudoCarros += totalPagar;
        } else {
            recaudoMotos += totalPagar;
        }


        tiposVehiculos[indice] = null;
        fechasIngreso[indice] = null;
        System.out.println("¡Celda liberada con éxito!\n");
    }


    public static void main(String[] args) {






    }
}