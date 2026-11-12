//go:build windows

// Lanzador del Sistema de Gestión y Seguimiento del Avance de Obras - DRACOR.
// Al ejecutarlo: inicia MySQL de XAMPP (si no está activo), compila el sistema
// la primera vez, lo inicia y abre el navegador en http://localhost:8080
package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/exec"
	"path/filepath"
	"sort"
	"strings"
	"syscall"
	"time"
)

const (
	urlSistema = "http://localhost:8080"
	jarNombre  = "sistema-obras-dracor.jar"
)

func main() {
	titulo()
	dir := directorioBase()
	os.Chdir(dir)

	// 1) Si el sistema ya está en marcha, solo se abre el navegador.
	if puertoAbierto("127.0.0.1:8080") {
		paso("El sistema ya está en ejecución. Abriendo el navegador…")
		abrirNavegador()
		return
	}

	// 2) MySQL (XAMPP)
	paso("Verificando MySQL de XAMPP (puerto 3306)…")
	if !puertoAbierto("127.0.0.1:3306") {
		if !iniciarMySQL() {
			fallo("No se pudo iniciar MySQL.\n   Abra el Panel de Control de XAMPP, pulse «Start» en MySQL y vuelva a ejecutar este programa.")
		}
	}
	ok("MySQL activo.")

	// 3) Java
	java := buscarJava()
	if java == "" {
		fallo("No se encontró Java (JDK 17 o superior).\n   Instale un JDK 17+ o configure la variable JAVA_HOME.")
	}
	ok("Java encontrado: " + java)

	// 4) Compilar si hace falta
	jar := filepath.Join(dir, "target", jarNombre)
	if _, err := os.Stat(jar); err != nil || fuentesMasNuevas(dir, jar) {
		paso("Compilando el sistema (la primera vez descarga librerías; requiere Internet y puede tardar unos minutos)…")
		mvn := buscarMaven()
		if mvn == "" {
			fallo("No se encontró Maven.\n   Abra el proyecto en NetBeans y use «Clean and Build», o instale Maven.")
		}
		if !compilar(mvn, dir, java) {
			fallo("La compilación falló. Revise los mensajes de arriba.")
		}
		ok("Compilación terminada.")
	}

	// 5) Iniciar el sistema
	paso("Iniciando el sistema DRACOR…")
	os.MkdirAll(filepath.Join(dir, "logs"), 0755)
	log, _ := os.Create(filepath.Join(dir, "logs", "sistema.log"))
	cmd := exec.Command(java, "-jar", jar)
	cmd.Dir = dir
	cmd.Stdout = log
	cmd.Stderr = log
	cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: true, CreationFlags: 0x08000000}
	if err := cmd.Start(); err != nil {
		fallo("No se pudo iniciar el sistema: " + err.Error())
	}

	hecho := make(chan struct{})
	go func() { cmd.Wait(); close(hecho) }()

	limite := time.Now().Add(150 * time.Second)
	for time.Now().Before(limite) {
		if puertoAbierto("127.0.0.1:8080") {
			ok("Sistema listo.")
			abrirNavegador()
			fmt.Println()
			fmt.Println("  El sistema sigue funcionando. Para APAGARLO cierre esta ventana")
			fmt.Println("  o pulse ENTER.")
			fmt.Println("  Usuario: admin   Contraseña: Admin2026")
			esperar(hecho)
			cmd.Process.Kill()
			return
		}
		select {
		case <-hecho:
			fallo("El sistema se detuvo al iniciar. Revise el archivo logs\\sistema.log")
		case <-time.After(1500 * time.Millisecond):
		}
		fmt.Print(".")
	}
	fallo("El sistema tardó demasiado en iniciar. Revise el archivo logs\\sistema.log")
}

func esperar(hecho chan struct{}) {
	entrada := make(chan struct{})
	go func() { bufio.NewReader(os.Stdin).ReadString('\n'); close(entrada) }()
	select {
	case <-hecho:
	case <-entrada:
	}
}

// ---------------------------------------------------------------- utilidades

func titulo() {
	fmt.Println("==============================================================")
	fmt.Println("  DRACOR · Sistema de Gestión y Seguimiento del Avance de Obras")
	fmt.Println("==============================================================")
}
func paso(m string) { fmt.Println("\n» " + m) }
func ok(m string)   { fmt.Println("  ✔ " + m) }
func fallo(m string) {
	fmt.Println("\n  ✖ " + m)
	fmt.Println("\nPulse ENTER para cerrar.")
	bufio.NewReader(os.Stdin).ReadString('\n')
	os.Exit(1)
}

func directorioBase() string {
	exe, err := os.Executable()
	if err != nil {
		d, _ := os.Getwd()
		return d
	}
	return filepath.Dir(exe)
}

func puertoAbierto(dir string) bool {
	c, err := net.DialTimeout("tcp", dir, 800*time.Millisecond)
	if err != nil {
		return false
	}
	c.Close()
	return true
}

func abrirNavegador() {
	c := exec.Command("rundll32", "url.dll,FileProtocolHandler", urlSistema)
	c.Start()
}

func existe(p string) bool {
	_, err := os.Stat(p)
	return err == nil
}

func iniciarMySQL() bool {
	raices := []string{`C:\xampp`, `D:\xampp`, `E:\xampp`, `C:\Archivos de programa\xampp`}
	if x := os.Getenv("XAMPP_HOME"); x != "" {
		raices = append([]string{x}, raices...)
	}
	for _, r := range raices {
		mysqld := filepath.Join(r, "mysql", "bin", "mysqld.exe")
		ini := filepath.Join(r, "mysql", "bin", "my.ini")
		if !existe(mysqld) {
			continue
		}
		paso("Iniciando MySQL desde " + r + "…")
		c := exec.Command(mysqld, "--defaults-file="+ini, "--standalone")
		c.Dir = filepath.Join(r, "mysql", "bin")
		c.SysProcAttr = &syscall.SysProcAttr{HideWindow: true, CreationFlags: 0x08000000 | 0x00000200}
		if err := c.Start(); err != nil {
			continue
		}
		for i := 0; i < 40; i++ {
			if puertoAbierto("127.0.0.1:3306") {
				return true
			}
			time.Sleep(time.Second)
		}
	}
	return false
}

func buscarJava() string {
	if jh := os.Getenv("JAVA_HOME"); jh != "" {
		p := filepath.Join(jh, "bin", "java.exe")
		if existe(p) {
			return p
		}
	}
	if p, err := exec.LookPath("java"); err == nil {
		return p
	}
	patrones := []string{
		`C:\Program Files\Java\jdk*\bin\java.exe`,
		`C:\Program Files\Eclipse Adoptium\jdk*\bin\java.exe`,
		`C:\Program Files\Microsoft\jdk*\bin\java.exe`,
		`C:\Program Files\Amazon Corretto\jdk*\bin\java.exe`,
		`C:\Program Files\Zulu\zulu*\bin\java.exe`,
		`C:\Program Files\BellSoft\LibericaJDK*\bin\java.exe`,
	}
	return primeraCoincidencia(patrones)
}

func buscarMaven() string {
	if mh := os.Getenv("MAVEN_HOME"); mh != "" {
		p := filepath.Join(mh, "bin", "mvn.cmd")
		if existe(p) {
			return p
		}
	}
	if p, err := exec.LookPath("mvn.cmd"); err == nil {
		return p
	}
	if p, err := exec.LookPath("mvn"); err == nil {
		return p
	}
	patrones := []string{
		`C:\Program Files\NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\Program Files\Apache NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\Program Files\Apache\NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\Program Files (x86)\NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\Users\*\AppData\Local\Programs\NetBeans*\netbeans\java\maven\bin\mvn.cmd`,
		`C:\apache-maven*\bin\mvn.cmd`,
		`C:\Program Files\Apache\maven*\bin\mvn.cmd`,
	}
	return primeraCoincidencia(patrones)
}

func primeraCoincidencia(patrones []string) string {
	var todos []string
	for _, p := range patrones {
		m, _ := filepath.Glob(p)
		todos = append(todos, m...)
	}
	if len(todos) == 0 {
		return ""
	}
	sort.Strings(todos)
	return todos[len(todos)-1]
}

func compilar(mvn, dir, java string) bool {
	c := exec.Command("cmd", "/C", mvn, "-q", "-DskipTests", "package")
	c.Dir = dir
	c.Stdout = os.Stdout
	c.Stderr = os.Stderr
	env := os.Environ()
	// Maven necesita JAVA_HOME: se deduce de la ruta de java.exe
	if os.Getenv("JAVA_HOME") == "" {
		home := filepath.Dir(filepath.Dir(java))
		env = append(env, "JAVA_HOME="+home)
	}
	c.Env = env
	if err := c.Run(); err != nil {
		return false
	}
	return existe(filepath.Join(dir, "target", jarNombre))
}

// fuentesMasNuevas indica si algún archivo fuente es más reciente que el jar.
func fuentesMasNuevas(dir, jar string) bool {
	info, err := os.Stat(jar)
	if err != nil {
		return true
	}
	limite := info.ModTime()
	nuevo := false
	filepath.Walk(filepath.Join(dir, "src"), func(p string, i os.FileInfo, e error) error {
		if e == nil && !i.IsDir() && i.ModTime().After(limite) && !strings.HasSuffix(p, ".log") {
			nuevo = true
			return filepath.SkipAll
		}
		return nil
	})
	if i, e := os.Stat(filepath.Join(dir, "pom.xml")); e == nil && i.ModTime().After(limite) {
		nuevo = true
	}
	return nuevo
}
