package com.jdrg.prcalculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.jdrg.prcalculadora.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnPunto.setOnClickListener(this)
        binding.btnNumberZero.setOnClickListener(this)
        binding.btnNumberOne.setOnClickListener(this)
        binding.btnNumberTwo.setOnClickListener(this)
        binding.btnNumberThree.setOnClickListener(this)
        binding.btnNumberFour.setOnClickListener(this)
        binding.btnNumberFive.setOnClickListener(this)
        binding.btnNumberSix.setOnClickListener(this)
        binding.btnNumberSeven.setOnClickListener(this)
        binding.btnNumberEight.setOnClickListener(this)
        binding.btnNumberNine.setOnClickListener(this)
        binding.btnBorrar.setOnClickListener(this)
        binding.btnBorrarTodo.setOnClickListener(this)
        binding.btnSumar.setOnClickListener(this)
        binding.btnRestar.setOnClickListener(this)
        binding.btnMultiplicar.setOnClickListener(this)
        binding.btnDividir.setOnClickListener(this)
        binding.btnIgual.setOnClickListener(this)
        binding.btnParentesisAbre.setOnClickListener(this)
        binding.btnParentesisCierre.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnNumberZero -> {
                recibirNumero("0")
            }

            R.id.btnNumberOne -> {
                recibirNumero("1")
            }

            R.id.btnNumberTwo -> {
                recibirNumero("2")
            }

            R.id.btnNumberThree -> {
                recibirNumero("3")
            }

            R.id.btnNumberFour -> {
                recibirNumero("4")
            }

            R.id.btnNumberFive -> {
                recibirNumero("5")
            }

            R.id.btnNumberSix -> {
                recibirNumero("6")
            }

            R.id.btnNumberSeven -> {
                recibirNumero("7")
            }

            R.id.btnNumberEight -> {
                recibirNumero("8")
            }

            R.id.btnNumberNine -> {
                recibirNumero("9")
            }

            R.id.btnSumar -> {
                recibirOperador("+")
            }

            R.id.btnRestar -> {
                recibirOperador("-")
            }

            R.id.btnMultiplicar -> {
                recibirOperador("*")
            }

            R.id.btnDividir -> {
                recibirOperador("/")
            }

            R.id.btnIgual -> {
                val expr = binding.textViewLabel.text.toString()
                val resultado = evaluar(expr)
                binding.textViewLabel.text = resultado.toString()
            }

            R.id.btnParentesisAbre -> {
                recibirOperador("(")
            }

            R.id.btnParentesisCierre -> {
                recibirOperador(")")
            }

            R.id.btnPunto -> {
                recibirOperador(".")
            }

            R.id.btnIgual -> {
                val expr = binding.textViewLabel.text.toString()
                val resultado = evaluar(expr)
                binding.textViewLabel.text = resultado.toString()
            }

            R.id.btnBorrar -> {
                borrarNumero()
            }

            R.id.btnBorrarTodo -> {
                borrarTodo()
            }

        }
    }

    private fun recibirNumero(numero: String) {
        binding.textViewLabel.append(numero)
    }

    private fun recibirOperador(operador: String) {
        binding.textViewLabel.append(operador)
    }

    private fun evaluar(expr: String): Double {
        // Verificar si la expresión termina con un operador
        if (!verificarInicioFinalOperador(expr)) {
            return Double.NaN
        }

        // Verificar si hay paréntesis consecutivos sin un operador entre ellos
        if (!verificarParentesisConsecutivos(expr)) {
            return Double.NaN
        }

        val numeros = mutableListOf<Double>()
        val operadores = mutableListOf<Char>()

        var i = 0
        while (i < expr.length) {
            //Verifica que si el carácter actual es un espacio en blanco, se ignora y se pasa al continuo.
            if (expr[i].isWhitespace()) {
                i++
                continue
            }

            // Identifica y extrae números de la expresión, con el fin de manejar tanto números enteros como decimales.
            if (expr[i].isDigit() || expr[i] == '.' || (expr[i] == '-' && (i == 0 || expr[i - 1] == '('))) {
                var buffer = ""
                if (expr[i] == '-') {
                    buffer += expr[i]
                    i++
                }
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    buffer += expr[i]
                    i++
                }
                numeros.add(buffer.toDouble())
            }
            // Manejar paréntesis de apertura '('
            else if (expr[i] == '(') {
                if (!verificarOperadorAntesParentesis(expr, i)) {
                    return Double.NaN
                }
                operadores.add(expr[i])
                i++
            }

            // Manejar paréntesis de cierre ')'
            else if (expr[i] == ')') {
                if (!verificarParentesis(operadores)) {
                    return Double.NaN
                }
                while (operadores.last() != '(') {
                    //Procesamos las operaciones pendientes dentro de la subexpresión.
                    procesarOperacion(numeros, operadores)
                }
                operadores.removeAt(operadores.size - 1) // remove '('
                i++
            }

            //Procesa todas las operaciones con mayor o igual prioridad antes de añadir el nuevo operador a la lista.
            else if (expr[i] == '+' || expr[i] == '-' || expr[i] == '*' || expr[i] == '/') {
                if (!verificarOperador(expr, i)) {
                    return Double.NaN
                }
                while (operadores.isNotEmpty() && operadores.last() != '(' && prioridad(expr[i]) <= prioridad(
                        operadores.last()
                    )
                ) {
                    procesarOperacion(numeros, operadores)
                }
                operadores.add(expr[i])
                i++
            }
        }

        // Verificar si todos los paréntesis están emparejados correctamente
        if (!verificaParejaParentesis(operadores)) {
            return Double.NaN
        }

        // Procesar las operaciones restantes y devolver el resultado final
        while (operadores.isNotEmpty()) {
            procesarOperacion(numeros, operadores)
        }

        return numeros.last()
    }

    private fun prioridad(op: Char): Int {
        return when (op) {
            '+' -> 1
            '-' -> 1
            '*' -> 2
            '/' -> 2
            else -> -1
        }
    }

    private fun procesarOperacion(numeros: MutableList<Double>, operadores: MutableList<Char>) {
        val num1 = numeros.removeAt(numeros.size - 1)
        val num2 = numeros.removeAt(numeros.size - 1)

        val resultado = when (operadores.removeAt(operadores.size - 1)) {
            '+' -> num2 + num1
            '-' -> num2 - num1
            '*' -> num2 * num1
            '/' -> {
                if (num1 == 0.0) {
                    Toast.makeText(this, "Error: No se puede dividir por cero", Toast.LENGTH_SHORT).show()
                    Double.NaN
                } else {
                    num2 / num1
                }
            }

            else -> 0.0
        }

        numeros.add(resultado)
    }

    private fun verificarInicioFinalOperador(expr: String): Boolean {
        if (expr.isNotEmpty() && "+-*/".contains(expr.last())) {
            Toast.makeText(this, "Error: La expresion termina con un operador", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (expr.isNotEmpty() && "+*/".contains(expr.first())) {
            Toast.makeText(this, "La expresion empieza con un operador", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun verificarParentesisConsecutivos(expr: String): Boolean {
        for (i in 0 until expr.length - 1) {
            if (expr[i] == ')' && expr[i + 1] == '(') {
                Toast.makeText(
                    this,
                    "Error: Dos grupos de paréntesis sin operador entre medio",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return true
    }

    private fun verificarOperadorAntesParentesis(expr: String, i: Int): Boolean {
        if (i > 0 && expr[i - 1].isDigit() && expr[i + 1] != '-' && expr[i + 1] != '+') {
            Toast.makeText(
                this,
                "Error: Número seguido de paréntesis de apertura sin operador",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun verificarParentesis(operadores: MutableList<Char>): Boolean {
        if (operadores.isEmpty() || !operadores.contains('(')) {
            Toast.makeText(
                this,
                "Error: Paréntesis de cierre sin paréntesis de apertura correspondiente",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun verificaParejaParentesis(operadores: MutableList<Char>): Boolean {
        if (operadores.contains('(')) {
            Toast.makeText(
                this,
                "Error: Paréntesis de apertura sin paréntesis de cierre correspondiente",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun verificarOperador(expr: String, i: Int): Boolean {
        if (i > 0 && "+-*/".contains(expr[i - 1]) && expr[i] != '-') {
            Toast.makeText(this, "Error: Dos operadores consecutivos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun borrarNumero() {
        val text = binding.textViewLabel.text.toString()
        if (text.isNotEmpty()) {
            binding.textViewLabel.text = text.substring(0, text.length - 1)
        }
    }

    private fun borrarTodo() {
        binding.textViewLabel.text = ""
    }
}